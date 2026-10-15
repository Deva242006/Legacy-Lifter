package com.legacylifter.scoring.service;

import com.legacylifter.common.entity.AnalysisRun;
import com.legacylifter.common.entity.CodeIssue;
import com.legacylifter.common.repository.CodeIssueRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Scores the Maintainability dimension (30% of overall score).
 *
 * Factors:
 * - Number of issues relative to total files (density)
 * - Prevalence of raw-type and old-switch issues (readability)
 * - File-level clustering (many issues in same file = low cohesion)
 */
@Component
public class MaintainabilityScorer {

    private final CodeIssueRepository issueRepository;

    public MaintainabilityScorer(CodeIssueRepository issueRepository) {
        this.issueRepository = issueRepository;
    }

    public float score(AnalysisRun run) {
        if (run == null || run.getTotalFiles() == 0) return 100f;

        List<CodeIssue> issues = issueRepository.findByAnalysisRunId(run.getId());
        if (issues.isEmpty()) return 100f;

        int totalFiles  = run.getTotalFiles();
        int issueCount  = issues.size();

        // Density penalty: each issue beyond 2 per file drops the score
        float density      = (float) issueCount / totalFiles;
        float densityScore = Math.max(0f, 100f - (density * 12f));

        // Readability penalty: raw types + old switch = hard-to-read code
        long readabilityHits = issues.stream()
                .filter(i -> "RAW_TYPES".equals(i.getCategory())
                          || "OLD_SWITCH".equals(i.getCategory()))
                .count();
        float readabilityPenalty = Math.min(30f, readabilityHits * 3f);

        // Clustering penalty: files with more than 5 issues each
        Map<String, Long> perFile = issues.stream()
                .collect(Collectors.groupingBy(CodeIssue::getFilePath, Collectors.counting()));
        long clusteredFiles = perFile.values().stream().filter(c -> c > 5).count();
        float clusterPenalty = Math.min(20f, clusteredFiles * 5f);

        float raw = densityScore - readabilityPenalty - clusterPenalty;
        return clamp(raw);
    }

    private float clamp(float v) {
        return Math.max(0f, Math.min(100f, v));
    }
}
