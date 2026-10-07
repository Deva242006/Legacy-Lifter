package com.legacylifter.event;

import java.time.Instant;
import java.util.UUID;

public record ScoreCalculatedEvent(
        UUID eventId,
        UUID projectId,
        float overallScore,
        String grade,
        Instant timestamp
) implements DomainEvent {

    public ScoreCalculatedEvent(UUID projectId, float overallScore, String grade) {
        this(UUID.randomUUID(), projectId, overallScore, grade, Instant.now());
    }

    @Override
    public String topic() {
        return "score.calculated";
    }
}
