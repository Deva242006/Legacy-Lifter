package com.legacylifter.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.stereotype.Component;

/**
 * Custom Micrometer metrics tracker for LegacyLifter platform telemetry.
 */
@Component
public class CustomMetrics {

    private final Counter analysisRunCounter;
    private final Counter issuesDetectedCounter;
    private final Counter modernizationsAppliedCounter;
    private final Counter prCreatedCounter;

    public CustomMetrics(MeterRegistry registry) {
        this.analysisRunCounter = Counter.builder("legacylifter.analysis.runs")
                .description("Total static analysis executions")
                .register(registry);

        this.issuesDetectedCounter = Counter.builder("legacylifter.issues.detected")
                .description("Total code anti-patterns detected")
                .register(registry);

        this.modernizationsAppliedCounter = Counter.builder("legacylifter.modernizations.applied")
                .description("Total Java 21 modernizations applied")
                .register(registry);

        this.prCreatedCounter = Counter.builder("legacylifter.github.prs")
                .description("Total GitHub pull requests created")
                .register(registry);
    }

    public void incrementAnalysisRuns() { analysisRunCounter.increment(); }
    public void incrementIssues(int count) { issuesDetectedCounter.increment(count); }
    public void incrementModernizations() { modernizationsAppliedCounter.increment(); }
    public void incrementPullRequests() { prCreatedCounter.increment(); }
}
