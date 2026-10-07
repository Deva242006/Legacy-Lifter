package com.legacylifter.testgen.dto;

import com.legacylifter.common.entity.GeneratedTest;

import java.time.Instant;
import java.util.UUID;

public class TestGenDtos {

    public record GeneratedTestResponse(
            UUID id,
            UUID projectId,
            String targetClass,
            String targetMethod,
            String testCode,
            String testFramework,
            double coverageEstimate,
            double mutationScore,
            Instant generatedAt
    ) {
        public static GeneratedTestResponse from(GeneratedTest t) {
            return new GeneratedTestResponse(
                    t.getId(),
                    t.getProject().getId(),
                    t.getTargetClass(),
                    t.getTargetMethod(),
                    t.getTestCode(),
                    t.getTestFramework(),
                    t.getCoverageEstimate(),
                    t.getMutationScore(),
                    t.getGeneratedAt()
            );
        }
    }
}
