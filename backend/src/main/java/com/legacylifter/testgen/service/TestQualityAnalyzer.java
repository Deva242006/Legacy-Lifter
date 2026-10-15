package com.legacylifter.testgen.service;

import com.legacylifter.common.entity.GeneratedTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Analyses the quality of LLM-generated tests.
 *
 * <p>Performs lightweight static checks without executing the tests:
 * <ul>
 *   <li>Assertion density – ratio of assertion calls to total lines</li>
 *   <li>Test method coverage – number of distinct {@code @Test} methods</li>
 *   <li>Parameterized test usage – bonus for {@code @ParameterizedTest}</li>
 *   <li>Edge-case coverage – presence of null/empty/boundary tests</li>
 * </ul>
 * Returns a quality score in [0.0, 1.0] and a short feedback summary.</p>
 */
@Component
public class TestQualityAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(TestQualityAnalyzer.class);

    public record QualityReport(
            float qualityScore,
            int testMethodCount,
            int assertionCount,
            boolean hasParameterizedTests,
            boolean hasEdgeCaseTests,
            String feedback
    ) {}

    /**
     * Analyse a single generated test's code.
     *
     * @param test the {@link GeneratedTest} to evaluate
     * @return a {@link QualityReport} with scores and human-readable feedback
     */
    public QualityReport analyse(GeneratedTest test) {
        String code = test.getTestCode();
        if (code == null || code.isBlank()) {
            return new QualityReport(0f, 0, 0, false, false, "No test code found.");
        }

        String[] lines = code.split("\n");
        int totalLines = lines.length;

        // Count @Test and @ParameterizedTest annotations
        long testMethods = countOccurrences(code, "@Test");
        long parameterizedTests = countOccurrences(code, "@ParameterizedTest");

        // Count assertion calls (AssertJ + JUnit 5 styles)
        long assertions = countOccurrences(code, "assertThat")
                        + countOccurrences(code, "assertEquals")
                        + countOccurrences(code, "assertNotNull")
                        + countOccurrences(code, "assertThrows")
                        + countOccurrences(code, "assertAll")
                        + countOccurrences(code, "assertTrue")
                        + countOccurrences(code, "assertFalse");

        // Detect edge-case coverage indicators
        boolean hasEdgeCases = code.contains("null")
                || code.contains("empty")
                || code.contains("blank")
                || code.contains("\"\"")
                || code.contains("List.of()")
                || code.contains("assertThrows");

        // Score components
        float assertionDensity = totalLines > 0 ? Math.min(1f, (float) assertions / (totalLines * 0.3f)) : 0f;
        float methodScore      = Math.min(1f, testMethods / 5f);   // target: 5+ test methods
        float edgeBonus        = hasEdgeCases ? 0.15f : 0f;
        float paramBonus       = parameterizedTests > 0 ? 0.10f : 0f;

        float raw = (assertionDensity * 0.40f)
                  + (methodScore      * 0.35f)
                  + edgeBonus
                  + paramBonus;

        float qualityScore = Math.min(1f, raw);

        String feedback = buildFeedback(testMethods, assertions, hasEdgeCases, parameterizedTests > 0);

        log.debug("Test quality analysis for {}.{}: score={} methods={} assertions={}",
                test.getTargetClass(), test.getTargetMethod(), qualityScore, testMethods, assertions);

        return new QualityReport(
                qualityScore,
                (int) testMethods,
                (int) assertions,
                parameterizedTests > 0,
                hasEdgeCases,
                feedback
        );
    }

    /**
     * Analyse a batch of tests and return the average quality score.
     */
    public float averageQuality(List<GeneratedTest> tests) {
        if (tests == null || tests.isEmpty()) return 0f;
        return (float) tests.stream()
                .mapToDouble(t -> analyse(t).qualityScore())
                .average()
                .orElse(0.0);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private long countOccurrences(String text, String token) {
        int count = 0;
        int idx   = 0;
        while ((idx = text.indexOf(token, idx)) != -1) {
            count++;
            idx += token.length();
        }
        return count;
    }

    private String buildFeedback(long methods, long assertions, boolean edgeCases, boolean parameterized) {
        StringBuilder sb = new StringBuilder();
        if (methods < 3) sb.append("⚠ Add more test methods (found ").append(methods).append("). ");
        if (assertions < methods) sb.append("⚠ Some test methods lack assertions. ");
        if (!edgeCases) sb.append("⚠ Add null/empty/boundary test cases. ");
        if (!parameterized) sb.append("💡 Consider @ParameterizedTest for data-driven scenarios. ");
        if (sb.isEmpty()) sb.append("✅ Test quality looks good!");
        return sb.toString().trim();
    }
}
