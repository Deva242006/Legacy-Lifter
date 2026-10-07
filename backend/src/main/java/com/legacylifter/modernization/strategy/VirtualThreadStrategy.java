package com.legacylifter.modernization.strategy;

import com.legacylifter.common.entity.CodeIssue;
import org.springframework.stereotype.Component;

/**
 * Modernizes platform thread pools and synchronized blocking calls to Java 21 Virtual Threads.
 */
@Component
public final class VirtualThreadStrategy implements ModernizationStrategy {

    @Override
    public boolean supports(CodeIssue issue) {
        return issue.getCategory() == CodeIssue.IssueCategory.OLD_CONCURRENCY
                || issue.getCategory() == CodeIssue.IssueCategory.BLOCKING_IO;
    }

    @Override
    public String modernize(String originalCode, String ragContext) {
        if (originalCode.contains("Executors.newFixedThreadPool") || originalCode.contains("Executors.newCachedThreadPool")) {
            return "// Modernized using Java 21 Virtual Threads (JEP 444)\n" +
                    originalCode.replaceAll("Executors\\.new(Fixed|Cached)ThreadPool\\([^\\)]*\\)",
                            "Executors.newVirtualThreadPerTaskExecutor()");
        }
        return "// Modernized concurrency:\ntry (var executor = Executors.newVirtualThreadPerTaskExecutor()) {\n    " +
                originalCode.indent(4).trim() + "\n}";
    }

    @Override
    public String strategyName() {
        return "VIRTUAL_THREADS_STRATEGY";
    }
}
