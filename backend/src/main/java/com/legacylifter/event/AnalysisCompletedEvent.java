package com.legacylifter.event;

import java.time.Instant;
import java.util.UUID;

public record AnalysisCompletedEvent(
        UUID eventId,
        UUID projectId,
        UUID analysisRunId,
        int issuesFound,
        int filesAnalyzed,
        long durationMs,
        Instant timestamp
) implements DomainEvent {

    public AnalysisCompletedEvent(UUID projectId, UUID analysisRunId,
                                   int issuesFound, int filesAnalyzed, long durationMs) {
        this(UUID.randomUUID(), projectId, analysisRunId, issuesFound, filesAnalyzed,
                durationMs, Instant.now());
    }

    @Override
    public String topic() {
        return "analysis.completed";
    }
}
