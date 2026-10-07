package com.legacylifter.common.entity;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Tracks debt score trends over time for a project.
 * Enables dashboard line charts showing improvement/degradation.
 */
@Entity
@Table(name = "score_trends", indexes = {
        @Index(name = "idx_score_trend_project", columnList = "project_id"),
        @Index(name = "idx_score_trend_recorded_at", columnList = "recorded_at")
})
public class ScoreTrend extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "overall_score", nullable = false)
    private float overallScore;

    @Column(name = "maintainability")
    private float maintainability;

    @Column(name = "security")
    private float security;

    @Column(name = "performance")
    private float performance;

    @Column(name = "modernity")
    private float modernity;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt = Instant.now();

    // ============================================================
    // Getters & Setters
    // ============================================================

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public float getOverallScore() { return overallScore; }
    public void setOverallScore(float overallScore) { this.overallScore = overallScore; }

    public float getMaintainability() { return maintainability; }
    public void setMaintainability(float maintainability) { this.maintainability = maintainability; }

    public float getSecurity() { return security; }
    public void setSecurity(float security) { this.security = security; }

    public float getPerformance() { return performance; }
    public void setPerformance(float performance) { this.performance = performance; }

    public float getModernity() { return modernity; }
    public void setModernity(float modernity) { this.modernity = modernity; }

    public Instant getRecordedAt() { return recordedAt; }
    public void setRecordedAt(Instant recordedAt) { this.recordedAt = recordedAt; }
}
