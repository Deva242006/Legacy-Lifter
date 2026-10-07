package com.legacylifter.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Dual event publisher that supports both Kafka and Spring ApplicationEvents.
 *
 * <p>When {@code legacylifter.events.use-kafka} is true, events are published
 * to Kafka topics. Otherwise, they are published as Spring ApplicationEvents
 * for in-process consumption.</p>
 *
 * <p>All events are also pushed to WebSocket subscribers for real-time
 * dashboard updates.</p>
 */
@Service
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    private final ApplicationEventPublisher springEventPublisher;
    private final SimpMessagingTemplate webSocketTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${legacylifter.events.use-kafka:false}")
    private boolean useKafka;

    public EventPublisher(ApplicationEventPublisher springEventPublisher,
                          SimpMessagingTemplate webSocketTemplate,
                          KafkaTemplate<String, Object> kafkaTemplate) {
        this.springEventPublisher = springEventPublisher;
        this.webSocketTemplate = webSocketTemplate;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publish a domain event to the appropriate event bus and WebSocket.
     */
    public void publish(DomainEvent event) {
        log.info("Publishing event: {} for project {}", event.getClass().getSimpleName(), event.projectId());

        if (useKafka) {
            kafkaTemplate.send(event.topic(), event.projectId().toString(), event);
            log.debug("Event published to Kafka topic: {}", event.topic());
        } else {
            springEventPublisher.publishEvent(event);
            log.debug("Event published via Spring ApplicationEvents");
        }

        // Always push to WebSocket for real-time frontend updates
        webSocketTemplate.convertAndSend(
                "/topic/projects/" + event.projectId() + "/events",
                event
        );
    }
}
