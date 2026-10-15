package com.legacylifter.scoring.service;

import com.legacylifter.common.entity.AnalysisRun;
import com.legacylifter.common.entity.CodeIssue;
import com.legacylifter.common.repository.CodeIssueRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scores the Security dimension (25% of overall score).
 *
 * Penalises:
 * - BLOCKING_IO in thread-pool contexts (resource exhaustion)
 * - OLD_CONCURRENCY patterns (race conditions, deadlocks)
 * - HIGH/CRITICAL severity issues in general
 */
@Component
public class SecurityScorer {

    private final CodeIssueRepository issueRepository;

    public SecurityScorer(CodeIssueRepository issueRepository) {
        this.issueRepository = issueRepository;
    }

    public float score(AnalysisRun run) {
        if (run == null || run.getTotalFiles() == 0) return 100f;

        List<CodeIssue> issues = issueRepository.findByAnalysisRunId(run.getId());
        if (issues.isEmpty()) return 100f;

        float penalty = 0f;

        for (CodeIssue issue : issues) {
            penalty += switch (issue.getCategory()) {
                case "BLOCKING_IO"       -> 8f;   // can starve thread pool
                case "OLD_CONCURRENCY"   -> 10f;  // race / deadlock risk
                default -> switch (issue.getSeverity()) {
                    case "CRITICAL" -> 6f;
                    case "HIGH"     -> 3f;
                    case "MEDIUM"   -> 1f;
                    default         -> 0.5f;
                };
            };
        }

        // Cap penalty at 100 so we never go below 0
        float capped = Math.min(penalty, 100f);
        return clamp(100f - capped);
    }

    private float clamp(float v) {
        return Math.max(0f, Math.min(100f, v));
    }
}
