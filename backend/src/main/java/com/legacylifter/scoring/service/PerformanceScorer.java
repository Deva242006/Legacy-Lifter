package com.legacylifter.scoring.service;

import com.legacylifter.common.entity.AnalysisRun;
import com.legacylifter.common.entity.CodeIssue;
import com.legacylifter.common.repository.CodeIssueRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scores the Performance dimension (25% of overall score).
 *
 * Penalises:
 * - BLOCKING_IO in virtual-thread-hostile positions
 * - OLD_CONCURRENCY (platform threads, executor pools)
 * - STREAM_IMPROVEMENT misses (sub-optimal pipeline usage)
 * - OPTIONAL_MISUSE (unnecessary allocations / NPE risk)
 */
@Component
public class PerformanceScorer {

    private final CodeIssueRepository issueRepository;

    public PerformanceScorer(CodeIssueRepository issueRepository) {
        this.issueRepository = issueRepository;
    }

    public float score(AnalysisRun run) {
        if (run == null || run.getTotalFiles() == 0) return 100f;

        List<CodeIssue> issues = issueRepository.findByAnalysisRunId(run.getId());
        if (issues.isEmpty()) return 100f;

        float penalty = 0f;

        for (CodeIssue issue : issues) {
            penalty += switch (issue.getCategory()) {
                case "BLOCKING_IO"          -> 9f;   // direct throughput impact
                case "OLD_CONCURRENCY"      -> 7f;   // missed virtual-thread benefit
                case "STREAM_IMPROVEMENT"   -> 3f;   // inefficient pipelines
                case "OPTIONAL_MISUSE"      -> 2f;   // unnecessary object alloc
                case "RAW_TYPES"            -> 1f;   // erased generics = missed JIT opts
                default                     -> 0.5f;
            };
        }

        return clamp(100f - Math.min(penalty, 100f));
    }

    private float clamp(float v) {
        return Math.max(0f, Math.min(100f, v));
    }
}
