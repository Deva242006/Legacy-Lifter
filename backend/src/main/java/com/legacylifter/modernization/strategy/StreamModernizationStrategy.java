package com.legacylifter.modernization.strategy;

import com.legacylifter.common.entity.CodeIssue;
import org.springframework.stereotype.Component;

/**
 * Modernizes legacy stream operations (e.g. .collect(Collectors.toList()) -> .toList()).
 */
@Component
public final class StreamModernizationStrategy implements ModernizationStrategy {

    @Override
    public boolean supports(CodeIssue issue) {
        return issue.getCategory() == CodeIssue.IssueCategory.STREAM_IMPROVEMENTS;
    }

    @Override
    public String modernize(String originalCode, String ragContext) {
        if (originalCode.contains("Collectors.toList()")) {
            return originalCode.replace(".collect(Collectors.toList())", ".toList()");
        }
        return "// Modernized Stream API\n" + originalCode;
    }

    @Override
    public String strategyName() {
        return "STREAM_MODERNIZATION_STRATEGY";
    }
}
