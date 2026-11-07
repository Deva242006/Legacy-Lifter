package com.legacylifter.scoring.service;

import com.legacylifter.common.entity.AnalysisRun;
import com.legacylifter.common.entity.CodeIssue;
import com.legacylifter.common.repository.CodeIssueRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Scores the Modernity dimension (20% of overall score).
 *
 * Rewards code that already uses modern Java 17-21 idioms.
 * Penalises every pattern that has a direct Java-21 replacement:
 * - MISSING_RECORDS        → should use records
 * - MISSING_SEALED_CLASSES → should use sealed classes
 * - OLD_SWITCH            → should use pattern-matching switch
 * - ANONYMOUS_CLASSES     → should use lambdas
 * - RAW_TYPES             → should use generics
 * - STREAM_IMPROVEMENTS   → should use modern Stream APIs
 */
@Component
public class ModernityScorer {

    private static final Set<CodeIssue.IssueCategory> MODERNITY_CATEGORIES = Set.of(
            CodeIssue.IssueCategory.MISSING_RECORDS,
            CodeIssue.IssueCategory.MISSING_SEALED_CLASSES,
            CodeIssue.IssueCategory.OLD_SWITCH,
            CodeIssue.IssueCategory.ANONYMOUS_CLASSES,
            CodeIssue.IssueCategory.RAW_TYPES,
            CodeIssue.IssueCategory.STREAM_IMPROVEMENTS
    );

    private final CodeIssueRepository issueRepository;

    public ModernityScorer(CodeIssueRepository issueRepository) {
        this.issueRepository = issueRepository;
    }

    public float score(AnalysisRun run) {
        if (run == null || run.getTotalFiles() == 0) return 100f;

        List<CodeIssue> issues = issueRepository.findByAnalysisRunId(run.getId());
        if (issues.isEmpty()) return 100f;

        long modernityIssues = issues.stream()
                .filter(i -> MODERNITY_CATEGORIES.contains(i.getCategory()))
                .count();

        float penaltyPerIssue = 4f;
        float penalty = Math.min(100f, modernityIssues * penaltyPerIssue);

        return clamp(100f - penalty);
    }

    private float clamp(float v) {
        return Math.max(0f, Math.min(100f, Math.round(v * 10f) / 10f));
    }
}
