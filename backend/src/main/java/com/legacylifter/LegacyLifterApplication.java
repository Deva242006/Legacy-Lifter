package com.legacylifter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * LegacyLifter — AI-Powered Java Code Modernization & Technical Debt Eraser.
 *
 * <p>This platform analyzes legacy Java codebases (Java 8/11 era), detects outdated
 * patterns using AST-level analysis, and leverages AI (RAG + LLM) to automatically
 * modernize code to Java 21+ standards while measuring and reducing technical debt.</p>
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class LegacyLifterApplication {

    public static void main(String[] args) {
        SpringApplication.run(LegacyLifterApplication.class, args);
    }
}
