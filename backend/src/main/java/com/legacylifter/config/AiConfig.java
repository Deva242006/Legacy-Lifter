package com.legacylifter.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * AI configuration placeholder for Spring AI / LangChain4j setup.
 *
 * <p>Actual LLM beans are configured via application.yml properties.
 * This class exists for any programmatic AI configuration needed
 * beyond what Spring AI auto-configuration provides.</p>
 *
 * <p>Supports two profiles:
 * <ul>
 *   <li><b>local</b>: Uses Ollama (codellama:13b or deepseek-coder) for local LLM inference</li>
 *   <li><b>cloud</b>: Uses OpenAI (gpt-4o) for production-quality inference</li>
 * </ul></p>
 */
@Configuration
public class AiConfig {
    // Spring AI auto-configuration handles the ChatModel and EmbeddingModel beans
    // based on the active profile and application.yml properties.
    //
    // Custom beans will be added here when implementing the RAG pipeline (Day 3).
}
