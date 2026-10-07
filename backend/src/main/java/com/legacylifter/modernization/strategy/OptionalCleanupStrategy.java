package com.legacylifter.modernization.strategy;

import com.legacylifter.common.entity.CodeIssue;
import org.springframework.stereotype.Component;

/**
 * Modernizes unsafe direct Optional.get() calls to .orElseThrow() or .ifPresent().
 */
@Component
public final class OptionalCleanupStrategy implements ModernizationStrategy {

    @Override
    public boolean supports(CodeIssue issue) {
        return issue.getCategory() == CodeIssue.IssueCategory.OPTIONAL_MISUSE;
    }

    @Override
    public String modernize(String originalCode, String ragContext) {
        if (originalCode.endsWith(".get()")) {
            return originalCode.substring(0, originalCode.length() - 6) + ".orElseThrow()";
        }
        return "// Safe Optional usage:\n" + originalCode;
    }

    @Override
    public String strategyName() {
        return "OPTIONAL_CLEANUP_STRATEGY";
    }
}
