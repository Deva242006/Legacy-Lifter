package com.legacylifter.event;

import java.time.Instant;
import java.util.UUID;

public record ModernizationCompletedEvent(
        UUID eventId,
        UUID projectId,
        UUID modernizationRunId,
        int suggestionsGenerated,
        Instant timestamp
) implements DomainEvent {

    public ModernizationCompletedEvent(UUID projectId, UUID modernizationRunId,
                                        int suggestionsGenerated) {
        this(UUID.randomUUID(), projectId, modernizationRunId, suggestionsGenerated, Instant.now());
    }

    @Override
    public String topic() {
        return "modernization.completed";
    }
}
