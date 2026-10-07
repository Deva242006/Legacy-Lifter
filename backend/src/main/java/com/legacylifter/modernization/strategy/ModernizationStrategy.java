package com.legacylifter.modernization.strategy;

import com.legacylifter.common.entity.CodeIssue;

/**
 * Sealed interface for Java modernization strategies.
 */
public sealed interface ModernizationStrategy permits
        VirtualThreadStrategy,
        RecordConversionStrategy,
        PatternMatchingStrategy,
        StreamModernizationStrategy,
        OptionalCleanupStrategy,
        StructuredConcurrencyStrategy {

    /**
     * Determines whether this strategy can handle the given issue category/pattern.
     */
    boolean supports(CodeIssue issue);

    /**
     * Generates modern Java replacement code for the given legacy snippet.
     *
     * @param originalCode legacy code snippet
     * @param ragContext relevant migration documentation context retrieved from vector store
     * @return modernized code snippet
     */
    String modernize(String originalCode, String ragContext);

    /**
     * Strategy unique identifier name.
     */
    String strategyName();
}
