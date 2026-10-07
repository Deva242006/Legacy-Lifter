package com.legacylifter.analysis.detector;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.SynchronizedStmt;
import com.legacylifter.common.entity.CodeIssue;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Detects legacy concurrency patterns (platform thread pools, heavy synchronization)
 * that should be replaced with Java 21 Virtual Threads (Executors.newVirtualThreadPerTaskExecutor()).
 */
@Component
public final class OldConcurrencyDetector implements PatternDetector {

    private static final Set<String> LEGACY_THREAD_POOL_METHODS = Set.of(
            "newFixedThreadPool", "newCachedThreadPool", "newSingleThreadExecutor"
    );

    @Override
    public List<CodeIssue> detect(CompilationUnit cu, String relativeFilePath) {
        List<CodeIssue> issues = new ArrayList<>();

        // 1. Detect platform thread pools in Executors call
        cu.findAll(MethodCallExpr.class).forEach(call -> {
            if (LEGACY_THREAD_POOL_METHODS.contains(call.getNameAsString())) {
                call.getRange().ifPresent(range -> {
                    CodeIssue issue = new CodeIssue();
                    issue.setFilePath(relativeFilePath);
                    issue.setLineNumber(range.begin.line);
                    issue.setEndLineNumber(range.end.line);
                    issue.setCategory(CodeIssue.IssueCategory.OLD_CONCURRENCY);
                    issue.setSeverity(CodeIssue.IssueSeverity.HIGH);
                    issue.setPatternType(patternType());
                    issue.setDescription("Platform thread pool call 'Executors." + call.getNameAsString() + "(...)' detected. Modernize to Java 21 Virtual Threads: 'Executors.newVirtualThreadPerTaskExecutor()'.");
                    issue.setOriginalCode(call.toString());
                    issue.setSuggestedFix("Executors.newVirtualThreadPerTaskExecutor()");
                    issue.setAutoFixable(true);
                    issues.add(issue);
                });
            }
        });

        // 2. Detect explicit synchronized statements
        cu.findAll(SynchronizedStmt.class).forEach(syncStmt -> {
            syncStmt.getRange().ifPresent(range -> {
                CodeIssue issue = new CodeIssue();
                issue.setFilePath(relativeFilePath);
                issue.setLineNumber(range.begin.line);
                issue.setEndLineNumber(range.end.line);
                issue.setCategory(CodeIssue.IssueCategory.OLD_CONCURRENCY);
                issue.setSeverity(CodeIssue.IssueSeverity.MEDIUM);
                issue.setPatternType("SYNCHRONIZED_BLOCK");
                issue.setDescription("Synchronized block detected. Prefer java.util.concurrent ReentrantLock or Concurrent data structures to prevent virtual thread pinning.");
                issue.setOriginalCode(syncStmt.toString());
                issue.setSuggestedFix("ReentrantLock lock = new ReentrantLock(); lock.lock(); try { ... } finally { lock.unlock(); }");
                issue.setAutoFixable(false);
                issues.add(issue);
            });
        });

        return issues;
    }

    @Override
    public CodeIssue.IssueCategory category() {
        return CodeIssue.IssueCategory.OLD_CONCURRENCY;
    }

    @Override
    public String patternType() {
        return "LEGACY_THREAD_POOL";
    }
}
