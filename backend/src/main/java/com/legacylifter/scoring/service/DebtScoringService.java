package com.legacylifter.scoring.service;

import com.legacylifter.common.entity.*;
import com.legacylifter.common.exception.LegacyLifterException;
import com.legacylifter.common.repository.AnalysisRunRepository;
import com.legacylifter.common.repository.DebtScoreRepository;
import com.legacylifter.common.repository.ProjectRepository;
import com.legacylifter.common.repository.ScoreTrendRepository;
import com.legacylifter.event.EventPublisher;
import com.legacylifter.event.ScoreCalculatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Technical Debt Scoring Engine.
 *
 * <p>Calculates a 4-dimension debt score (Maintainability, Security, Performance, Modernity)
 * and assigns a letter grade (A+, A, B, C, D, F) based on per-category issue analysis.</p>
 */
@Service
public class DebtScoringService {

    private static final Logger log = LoggerFactory.getLogger(DebtScoringService.class);

    private final ProjectRepository projectRepository;
    private final AnalysisRunRepository analysisRunRepository;
    private final DebtScoreRepository debtScoreRepository;
    private final ScoreTrendRepository scoreTrendRepository;
    private final EventPublisher eventPublisher;
    private final MaintainabilityScorer maintainabilityScorer;
    private final SecurityScorer securityScorer;
    private final PerformanceScorer performanceScorer;
    private final ModernityScorer modernityScorer;

    public DebtScoringService(ProjectRepository projectRepository,
                              AnalysisRunRepository analysisRunRepository,
                              DebtScoreRepository debtScoreRepository,
                              ScoreTrendRepository scoreTrendRepository,
                              EventPublisher eventPublisher,
                              MaintainabilityScorer maintainabilityScorer,
                              SecurityScorer securityScorer,
                              PerformanceScorer performanceScorer,
                              ModernityScorer modernityScorer) {
        this.projectRepository = projectRepository;
        this.analysisRunRepository = analysisRunRepository;
        this.debtScoreRepository = debtScoreRepository;
        this.scoreTrendRepository = scoreTrendRepository;
        this.eventPublisher = eventPublisher;
        this.maintainabilityScorer = maintainabilityScorer;
        this.securityScorer = securityScorer;
        this.performanceScorer = performanceScorer;
        this.modernityScorer = modernityScorer;
    }

    @Transactional
    public DebtScore calculateScore(UUID projectId, UUID analysisRunId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> LegacyLifterException.notFound("Project", projectId));

        AnalysisRun run = analysisRunRepository.findById(analysisRunId)
                .orElseThrow(() -> LegacyLifterException.notFound("AnalysisRun", analysisRunId));

        // Delegate to dedicated sub-scorers (each 0–100)
        float maintainability = maintainabilityScorer.score(run);
        float security        = securityScorer.score(run);
        float performance     = performanceScorer.score(run);
        float modernity       = modernityScorer.score(run);

        // Weighted overall: Maintainability 30%, Security 25%, Performance 25%, Modernity 20%
        float overall = round1dp(
                (maintainability * 0.30f)
              + (security        * 0.25f)
              + (performance     * 0.25f)
              + (modernity       * 0.20f));

        String grade = calculateGrade(overall);

        DebtScore score = new DebtScore();
        score.setProject(project);
        score.setAnalysisRunId(analysisRunId);
        score.setOverallScore(overall);
        score.setMaintainabilityScore(round1dp(maintainability));
        score.setSecurityScore(round1dp(security));
        score.setPerformanceScore(round1dp(performance));
        score.setModernityScore(round1dp(modernity));
        score.setCalculatedAt(Instant.now());

        Map<String, Object> breakdown = new HashMap<>();
        breakdown.put("grade", grade);
        breakdown.put("issueDensityPerFile",
                run.getTotalFiles() > 0
                    ? Math.round((double) run.getIssuesFound() / run.getTotalFiles() * 100.0) / 100.0
                    : 0);
        score.setBreakdown(breakdown);

        DebtScore saved = debtScoreRepository.save(score);

        // Record historical trend data point for charts
        ScoreTrend trend = new ScoreTrend();
        trend.setProject(project);
        trend.setOverallScore(overall);
        trend.setMaintainability(round1dp(maintainability));
        trend.setSecurity(round1dp(security));
        trend.setPerformance(round1dp(performance));
        trend.setModernity(round1dp(modernity));
        trend.setRecordedAt(Instant.now());
        scoreTrendRepository.save(trend);

        eventPublisher.publish(new ScoreCalculatedEvent(projectId, overall, grade));

        log.info("Calculated tech debt score for project {}: {} (Grade: {})", projectId, overall, grade);
        return saved;
    }

    private String calculateGrade(float score) {
        if (score >= 95) return "A+";
        if (score >= 85) return "A";
        if (score >= 75) return "B";
        if (score >= 65) return "C";
        if (score >= 50) return "D";
        return "F";
    }

    private float round1dp(float v) {
        return Math.round(v * 10f) / 10f;
    }
}
