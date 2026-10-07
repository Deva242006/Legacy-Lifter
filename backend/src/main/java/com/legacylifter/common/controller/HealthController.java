package com.legacylifter.common.controller;

import com.legacylifter.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Health check and system info endpoint.
 * Verifies the application is running and virtual threads are active.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "System", description = "Health check and system information")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Returns application health status and system info")
    public ApiResponse<Map<String, Object>> health() {
        Thread currentThread = Thread.currentThread();

        return ApiResponse.success(Map.of(
                "status", "UP",
                "application", "LegacyLifter",
                "version", "1.0.0-SNAPSHOT",
                "java", System.getProperty("java.version"),
                "virtualThread", currentThread.isVirtual(),
                "threadName", currentThread.getName(),
                "timestamp", Instant.now().toString()
        ));
    }

    @GetMapping("/info")
    @Operation(summary = "Application info", description = "Returns detailed application information")
    public ApiResponse<Map<String, Object>> info() {
        return ApiResponse.success(Map.of(
                "name", "LegacyLifter",
                "description", "AI-Powered Java Code Modernization & Technical Debt Eraser",
                "version", "1.0.0-SNAPSHOT",
                "features", Map.of(
                        "staticAnalysis", true,
                        "aiModernization", true,
                        "debtScoring", true,
                        "testGeneration", true,
                        "githubIntegration", true
                ),
                "runtime", Map.of(
                        "java", System.getProperty("java.version"),
                        "os", System.getProperty("os.name"),
                        "processors", Runtime.getRuntime().availableProcessors(),
                        "maxMemoryMB", Runtime.getRuntime().maxMemory() / (1024 * 1024)
                )
        ));
    }
}
