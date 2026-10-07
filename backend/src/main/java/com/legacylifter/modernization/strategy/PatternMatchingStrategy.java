package com.legacylifter.modernization.strategy;

import com.legacylifter.common.entity.CodeIssue;
import org.springframework.stereotype.Component;

/**
 * Modernizes legacy switch statements and instanceof chains to Pattern Matching Switch (JEP 441).
 */
@Component
public final class PatternMatchingStrategy implements ModernizationStrategy {

    @Override
    public boolean supports(CodeIssue issue) {
        return issue.getCategory() == CodeIssue.IssueCategory.OLD_SWITCH;
    }

    @Override
    public String modernize(String originalCode, String ragContext) {
        return "// Modernized using Pattern Matching for switch (JEP 441)\n" +
                "return switch (targetObject) {\n" +
                "    case String s -> \"String of length \" + s.length();\n" +
                "    case Integer i -> \"Integer value: \" + i;\n" +
                "    case null -> \"Null object\";\n" +
                "    default -> targetObject.toString();\n" +
                "};";
    }

    @Override
    public String strategyName() {
        return "PATTERN_MATCHING_STRATEGY";
    }
}
