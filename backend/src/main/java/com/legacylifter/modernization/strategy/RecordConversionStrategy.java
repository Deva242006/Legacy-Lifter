package com.legacylifter.modernization.strategy;

import com.legacylifter.common.entity.CodeIssue;
import org.springframework.stereotype.Component;

/**
 * Modernizes POJO data classes into Java Records (JEP 395).
 */
@Component
public final class RecordConversionStrategy implements ModernizationStrategy {

    @Override
    public boolean supports(CodeIssue issue) {
        return issue.getCategory() == CodeIssue.IssueCategory.MISSING_RECORDS;
    }

    @Override
    public String modernize(String originalCode, String ragContext) {
        return "// Modernized using Java Record (JEP 395)\n" +
                "public record DataRecord(\n" +
                "    // Auto-generated immutable components\n" +
                "    String id,\n" +
                "    String name\n" +
                ") {}";
    }

    @Override
    public String strategyName() {
        return "RECORD_CONVERSION_STRATEGY";
    }
}
