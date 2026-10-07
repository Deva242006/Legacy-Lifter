package com.legacylifter.modernization.service;

import com.legacylifter.common.entity.CodeIssue;
import com.legacylifter.modernization.strategy.ModernizationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for invoking LLMs (via Spring AI / LangChain4j) with RAG context to generate code modernizations.
 *
 * <p>Includes automatic strategy fallback when an LLM server is offline.</p>
 */
@Service
public class LlmService {

    private static final Logger log = LoggerFactory.getLogger(LlmService.class);

    @Autowired(required = false)
    private ChatModel chatModel;

    private final List<ModernizationStrategy> strategies;
    private final RagService ragService;

    public LlmService(List<ModernizationStrategy> strategies, RagService ragService) {
        this.strategies = strategies;
        this.ragService = ragService;
    }

    public record ModernizationResult(
            String modernizedCode,
            String explanation,
            double confidenceScore,
            String riskLevel,
            String strategyUsed
    ) {}

    /**
     * Generates modern Java replacement for a detected code issue.
     */
    public ModernizationResult generateModernization(CodeIssue issue) {
        String ragContext = ragService.retrieveContext(issue.getPatternType());

        // Find applicable Java 21 strategy
        ModernizationStrategy strategy = strategies.stream()
                .filter(s -> s.supports(issue))
                .findFirst()
                .orElse(null);

        if (chatModel != null) {
            try {
                String prompt = buildPrompt(issue, ragContext);
                String response = chatModel.call(prompt);
                if (response != null && !response.isBlank()) {
                    return new ModernizationResult(
                            cleanResponseCode(response),
                            "AI-generated modern Java transformation grounded in official migration guides.",
                            0.95,
                            "LOW",
                            strategy != null ? strategy.strategyName() : "SPRING_AI_LLM"
                    );
                }
            } catch (Exception e) {
                log.warn("Spring AI LLM call failed or offline. Falling back to Strategy engine: {}", e.getMessage());
            }
        }

        // Strategy fallback execution
        if (strategy != null) {
            String modernized = strategy.modernize(issue.getOriginalCode() != null ? issue.getOriginalCode() : issue.getDescription(), ragContext);
            return new ModernizationResult(
                    modernized,
                    "Deterministic transformation using strategy: " + strategy.strategyName(),
                    0.99,
                    "LOW",
                    strategy.strategyName()
            );
        }

        return new ModernizationResult(
                issue.getSuggestedFix() != null ? issue.getSuggestedFix() : "// Modernization suggestion unavailable",
                "Default fix suggestion.",
                0.80,
                "MEDIUM",
                "DEFAULT_FALLBACK"
        );
    }

    private String buildPrompt(CodeIssue issue, String ragContext) {
        return """
                You are a Java modernization expert. Refactor the following legacy Java code to modern Java 21 standard.
                
                RAG MIGRATION GUIDELINES:
                %s
                
                LEGACY CODE ISSUE:
                Category: %s
                Pattern: %s
                Code:
                ```java
                %s
                ```
                
                Requirements:
                1. Preserve behavior exactly.
                2. Use Java 21 features (Virtual Threads, Records, Pattern Matching, Stream.toList()).
                3. Output ONLY the modernized Java code block.
                """.formatted(ragContext, issue.getCategory(), issue.getPatternType(), issue.getOriginalCode());
    }

    private String cleanResponseCode(String response) {
        if (response.contains("```java")) {
            int start = response.indexOf("```java") + 7;
            int end = response.indexOf("```", start);
            if (end > start) {
                return response.substring(start, end).trim();
            }
        }
        return response.trim();
    }
}
