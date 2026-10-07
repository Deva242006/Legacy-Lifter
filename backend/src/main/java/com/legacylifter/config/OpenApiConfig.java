package com.legacylifter.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3.0 documentation configuration.
 * Swagger UI available at /swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI legacyLifterOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("LegacyLifter API")
                        .description("""
                                AI-Powered Java Code Modernization & Technical Debt Eraser.
                                
                                This API provides endpoints for:
                                - **Static Analysis**: Parse and detect outdated Java patterns
                                - **AI Modernization**: Generate modernized code using RAG + LLM
                                - **Technical Debt Scoring**: Multi-dimensional code quality assessment
                                - **Test Generation**: Automated JUnit 5 test creation
                                - **GitHub Integration**: Branch creation, PR workflow, webhooks
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("LegacyLifter Team")
                                .email("team@legacylifter.dev"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development"),
                        new Server().url("https://api.legacylifter.dev").description("Production")
                ));
    }
}
