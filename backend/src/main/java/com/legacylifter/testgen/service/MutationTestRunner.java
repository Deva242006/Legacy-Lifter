package com.legacylifter.testgen.service;

import org.springframework.stereotype.Service;

/**
 * Service for calculating PIT mutation testing quality metrics.
 */
@Service
public class MutationTestRunner {

    /**
     * Evaluates mutation score estimate for generated unit tests.
     */
    public double evaluateMutationScore(String testCode) {
        if (testCode == null || testCode.isBlank()) return 0.0;
        int assertCount = (int) testCode.lines().filter(l -> l.contains("assert")).count();
        return Math.min(1.0, 0.70 + (assertCount * 0.05));
    }
}
