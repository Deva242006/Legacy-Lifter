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
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
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
 * and assigns a letter grade (A+, A, B, C, D, F) based on static analysis issue density.</p>
 */
@Service
public class DebtScoringService {

    private static final Logger log = LoggerFactory.getLogger(DebtScoringService.class);

    private final ProjectRepository projectRepository;
    private final AnalysisRunRepository analysisRunRepository;
    private final DebtScoreRepository debtScoreRepository;
    private final ScoreTrendRepository scoreTrendRepository;
    private final EventPublisher eventPublisher;

    public DebtScoringService(ProjectRepository projectRepository,
                              AnalysisRunRepository analysisRunRepository,
                              DebtScoreRepository debtScoreRepository,
                              ScoreTrendRepository scoreTrendRepository,
                              EventPublisher eventPublisher) {
        this.projectRepository = projectRepository;
        this.analysisRunRepository = analysisRunRepository;
        this.debtScoreRepository = debtScoreRepository;
        this.scoreTrendRepository = scoreTrendRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public DebtScore calculateScore(UUID projectId, UUID analysisRunId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> LegacyLifterException.notFound("Project", projectId));

        AnalysisRun run = analysisRunRepository.findById(analysisRunId)
                .orElseThrow(() -> LegacyLifterException.notFound("AnalysisRun", analysisRunId));

        int totalFiles = Math.max(1, run.getTotalFiles());
        int issuesFound = run.getIssuesFound();

        double issueDensity = (double) issuesFound / totalFiles;

        // 4-Dimension scoring algorithm (0 - 100)
        double maintainability = Math.max(0.0, 100.0 - (issueDensity * 12.0));
        double security = Math.max(0.0, 100.0 - (issueDensity * 8.0));
        double performance = Math.max(0.0, 100.0 - (issueDensity * 15.0));
        double modernity = Math.max(0.0, 100.0 - (issueDensity * 20.0));

        // Weighted overall score calculation
        double overallScore = (maintainability * 0.35) + (modernity * 0.25) + (security * 0.20) + (performance * 0.20);
        overallScore = Math.round(overallScore * 10.0) / 10.0;

        DebtScore score = new DebtScore();
        score.setProject(project);
        score.setAnalysisRun(run);
        score.setOverallScore(overallScore);
        score.setMaintainabilityScore(Math.round(maintainability * 10.0) / 10.0);
        score.setSecurityScore(Math.round(security * 10.0) / 10.0);
        score.setPerformanceScore(Math.round(performance * 10.0) / 10.0);
        score.setModernityScore(Math.round(modernity * 10.0) / 10.0);
        score.setCalculatedAt(Instant.now());

        Map<String, Object> breakdown = new HashMap<>();
        breakdown.put("grade", calculateGrade(overallScore));
        breakdown.put("issueDensityPerFile", Math.round(issueDensity * 100.0) / 100.0);
        score.setBreakdown(breakdown);

        DebtScore saved = debtScoreRepository.save(score);

        // Record historical trend data point for charts
        ScoreTrend trend = new ScoreTrend();
        trend.setProject(project);
        trend.setOverallScore(overallScore);
        trend.setMaintainability(score.getMaintainabilityScore());
        trend.setSecurity(score.getSecurityScore());
        trend.setPerformance(score.getPerformanceScore());
        trend.setModernity(score.getModernityScore());
        trend.setRecordedAt(Instant.now());
        scoreTrendRepository.save(trend);

        eventPublisher.publish(new ScoreCalculatedEvent(projectId, saved.getId(), overallScore, calculateGrade(overallScore)));

        log.info("Calculated tech debt score for project {}: {} (Grade: {})", projectId, overallScore, calculateGrade(overallScore));
        return saved;
    }

    private String calculateGrade(double score) {
        if (score >= 95) return "A+";
        if (score >= 85) return "A";
        if (score >= 75) return "B";
        if (score >= 65) return "C";
        if (score >= 50) return "D";
        return "F";
    }
}
