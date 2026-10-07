package com.legacylifter.common.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

/**
 * Represents a technical debt score calculated for a project after analysis.
 * Covers 4 dimensions: maintainability, security, performance, and modernity.
 */
@Entity
@Table(name = "debt_scores")
public class DebtScore extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "analysis_run_id")
    private java.util.UUID analysisRunId;

    @Column(name = "overall_score", nullable = false)
    private float overallScore;

    @Column(name = "maintainability_score")
    private float maintainabilityScore;

    @Column(name = "security_score")
    private float securityScore;

    @Column(name = "performance_score")
    private float performanceScore;

    @Column(name = "modernity_score")
    private float modernityScore;

    @Column(name = "grade", length = 2)
    private String grade;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "breakdown", columnDefinition = "jsonb")
    private Map<String, Object> breakdown;

    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt = Instant.now();

    /**
     * Calculate the letter grade based on overall score.
     */
    public String calculateGrade() {
        return switch ((int) (overallScore / 10)) {
            case 10, 9 -> "A+";
            case 8 -> "A";
            case 7 -> "B";
            case 6 -> "C";
            case 5 -> "D";
            default -> "F";
        };
    }

    // ============================================================
    // Getters & Setters
    // ============================================================

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public java.util.UUID getAnalysisRunId() { return analysisRunId; }
    public void setAnalysisRunId(java.util.UUID analysisRunId) { this.analysisRunId = analysisRunId; }

    public float getOverallScore() { return overallScore; }
    public void setOverallScore(float overallScore) {
        this.overallScore = overallScore;
        this.grade = calculateGrade();
    }

    public float getMaintainabilityScore() { return maintainabilityScore; }
    public void setMaintainabilityScore(float maintainabilityScore) { this.maintainabilityScore = maintainabilityScore; }

    public float getSecurityScore() { return securityScore; }
    public void setSecurityScore(float securityScore) { this.securityScore = securityScore; }

    public float getPerformanceScore() { return performanceScore; }
    public void setPerformanceScore(float performanceScore) { this.performanceScore = performanceScore; }

    public float getModernityScore() { return modernityScore; }
    public void setModernityScore(float modernityScore) { this.modernityScore = modernityScore; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public Map<String, Object> getBreakdown() { return breakdown; }
    public void setBreakdown(Map<String, Object> breakdown) { this.breakdown = breakdown; }

    public Instant getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(Instant calculatedAt) { this.calculatedAt = calculatedAt; }
}
