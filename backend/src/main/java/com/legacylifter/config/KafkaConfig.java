package com.legacylifter.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka configuration — creates topics for the event-driven pipeline.
 *
 * <p>Topics are only created when the 'kafka' profile is active.
 * When Kafka is not available, the application falls back to Spring
 * ApplicationEvents (see EventPublisher).</p>
 */
@Configuration
@Profile("kafka")
public class KafkaConfig {

    public static final String TOPIC_ANALYSIS_COMPLETED = "analysis.completed";
    public static final String TOPIC_MODERNIZATION_COMPLETED = "modernization.completed";
    public static final String TOPIC_SCORE_CALCULATED = "score.calculated";
    public static final String TOPIC_TESTS_GENERATED = "tests.generated";

    @Bean
    public NewTopic analysisCompletedTopic() {
        return TopicBuilder.name(TOPIC_ANALYSIS_COMPLETED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic modernizationCompletedTopic() {
        return TopicBuilder.name(TOPIC_MODERNIZATION_COMPLETED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic scoreCalculatedTopic() {
        return TopicBuilder.name(TOPIC_SCORE_CALCULATED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic testsGeneratedTopic() {
        return TopicBuilder.name(TOPIC_TESTS_GENERATED)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
