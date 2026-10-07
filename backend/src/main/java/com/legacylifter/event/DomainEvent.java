package com.legacylifter.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Base interface for all domain events in the LegacyLifter pipeline.
 */
public sealed interface DomainEvent
        permits AnalysisCompletedEvent, ModernizationCompletedEvent,
                ScoreCalculatedEvent, TestsGeneratedEvent {

    UUID eventId();
    UUID projectId();
    Instant timestamp();
    String topic();
}
