package com.legacylifter.common.entity;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Represents an auto-generated JUnit 5 test for modernized code.
 */
@Entity
@Table(name = "generated_tests")
public class GeneratedTest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "modernization_run_id")
    private java.util.UUID modernizationRunId;

    @Column(name = "target_class", nullable = false)
    private String targetClass;

    @Column(name = "target_method")
    private String targetMethod;

    @Column(name = "test_code", columnDefinition = "TEXT", nullable = false)
    private String testCode;

    @Column(name = "test_framework")
    private String testFramework = "JUnit5";

    @Column(name = "coverage_estimate")
    private float coverageEstimate;

    @Column(name = "mutation_score")
    private float mutationScore;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt = Instant.now();

    // ============================================================
    // Getters & Setters
    // ============================================================

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public java.util.UUID getModernizationRunId() { return modernizationRunId; }
    public void setModernizationRunId(java.util.UUID modernizationRunId) { this.modernizationRunId = modernizationRunId; }

    public String getTargetClass() { return targetClass; }
    public void setTargetClass(String targetClass) { this.targetClass = targetClass; }

    public String getTargetMethod() { return targetMethod; }
    public void setTargetMethod(String targetMethod) { this.targetMethod = targetMethod; }

    public String getTestCode() { return testCode; }
    public void setTestCode(String testCode) { this.testCode = testCode; }

    public String getTestFramework() { return testFramework; }
    public void setTestFramework(String testFramework) { this.testFramework = testFramework; }

    public float getCoverageEstimate() { return coverageEstimate; }
    public void setCoverageEstimate(float coverageEstimate) { this.coverageEstimate = coverageEstimate; }

    public float getMutationScore() { return mutationScore; }
    public void setMutationScore(float mutationScore) { this.mutationScore = mutationScore; }

    public Instant getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(Instant generatedAt) { this.generatedAt = generatedAt; }
}
