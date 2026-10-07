package com.legacylifter.modernization.dto;

import com.legacylifter.common.entity.ModernizationRun;
import com.legacylifter.common.entity.ModernizationSuggestion;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class ModernizationDtos {

    public record ModernizationRunResponse(
            UUID id,
            UUID projectId,
            UUID analysisRunId,
            AnalysisRun.RunStatus status,
            int totalSuggestions,
            int appliedSuggestions,
            Instant startedAt,
            Instant completedAt
    ) {
        public static ModernizationRunResponse from(ModernizationRun r) {
            return new ModernizationRunResponse(
                    r.getId(),
                    r.getProject().getId(),
                    r.getAnalysisRunId(),
                    r.getStatus(),
                    r.getTotalSuggestions(),
                    r.getAppliedSuggestions(),
                    r.getStartedAt(),
                    r.getCompletedAt()
            );
        }
    }

    public record ModernizationSuggestionResponse(
            UUID id,
            UUID modernizationRunId,
            String strategyType,
            String filePath,
            String originalCode,
            String modernizedCode,
            String explanation,
            double confidenceScore,
            String riskLevel,
            boolean applied
    ) {
        public static ModernizationSuggestionResponse from(ModernizationSuggestion s) {
            return new ModernizationSuggestionResponse(
                    s.getId(),
                    s.getModernizationRun().getId(),
                    s.getStrategyType(),
                    s.getFilePath(),
                    s.getOriginalCode(),
                    s.getModernizedCode(),
                    s.getExplanation(),
                    s.getConfidenceScore(),
                    s.getRiskLevel(),
                    s.isApplied()
            );
        }
    }
}
