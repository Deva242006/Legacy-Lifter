package com.legacylifter.event;

import java.time.Instant;
import java.util.UUID;

public record TestsGeneratedEvent(
        UUID eventId,
        UUID projectId,
        int testsGenerated,
        float avgMutationScore,
        Instant timestamp
) implements DomainEvent {

    public TestsGeneratedEvent(UUID projectId, int testsGenerated, float avgMutationScore) {
        this(UUID.randomUUID(), projectId, testsGenerated, avgMutationScore, Instant.now());
    }

    @Override
    public String topic() {
        return "tests.generated";
    }
}
