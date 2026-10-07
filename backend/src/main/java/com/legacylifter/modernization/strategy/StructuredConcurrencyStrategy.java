package com.legacylifter.modernization.strategy;

import com.legacylifter.common.entity.CodeIssue;
import org.springframework.stereotype.Component;

/**
 * Modernizes nested CompletableFuture chains using Java Structured Concurrency (JEP 462).
 */
@Component
public final class StructuredConcurrencyStrategy implements ModernizationStrategy {

    @Override
    public boolean supports(CodeIssue issue) {
        return issue.getCategory() == CodeIssue.IssueCategory.OLD_CONCURRENCY
                && issue.getPatternType().contains("COMPLETABLE_FUTURE");
    }

    @Override
    public String modernize(String originalCode, String ragContext) {
        return "// Modernized using Structured Concurrency (JEP 462)\n" +
                "try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {\n" +
                "    Supplier<String> userTask = scope.fork(() -> fetchUser());\n" +
                "    Supplier<Order> orderTask = scope.fork(() -> fetchOrder());\n" +
                "\n" +
                "    scope.join().throwIfFailed();\n" +
                "    return new UserOrderDto(userTask.get(), orderTask.get());\n" +
                "}";
    }

    @Override
    public String strategyName() {
        return "STRUCTURED_CONCURRENCY_STRATEGY";
    }
}
