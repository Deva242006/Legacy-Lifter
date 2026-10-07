package com.legacylifter.scoring.dto;

import com.legacylifter.common.entity.DebtScore;
import com.legacylifter.common.entity.ScoreTrend;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class ScoringDtos {

    public record DebtScoreResponse(
            UUID id,
            UUID projectId,
            UUID analysisRunId,
            double overallScore,
            double maintainabilityScore,
            double securityScore,
            double performanceScore,
            double modernityScore,
            Map<String, Object> breakdown,
            Instant calculatedAt
    ) {
        public static DebtScoreResponse from(DebtScore s) {
            return new DebtScoreResponse(
                    s.getId(),
                    s.getProject().getId(),
                    s.getAnalysisRunId(),
                    s.getOverallScore(),
                    s.getMaintainabilityScore(),
                    s.getSecurityScore(),
                    s.getPerformanceScore(),
                    s.getModernityScore(),
                    s.getBreakdown(),
                    s.getCalculatedAt()
            );
        }
    }

    public record ScoreTrendResponse(
            UUID id,
            UUID projectId,
            double overallScore,
            double maintainability,
            double security,
            double performance,
            double modernity,
            Instant recordedAt
    ) {
        public static ScoreTrendResponse from(ScoreTrend t) {
            return new ScoreTrendResponse(
                    t.getId(),
                    t.getProject().getId(),
                    t.getOverallScore(),
                    t.getMaintainability(),
                    t.getSecurity(),
                    t.getPerformance(),
                    t.getModernity(),
                    t.getRecordedAt()
            );
        }
    }
}
