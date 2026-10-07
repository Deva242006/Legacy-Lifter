package com.legacylifter.common.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a modernization execution triggered after analysis.
 * Links analysis results to AI-generated modernization suggestions.
 */
@Entity
@Table(name = "modernization_runs")
public class ModernizationRun extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "analysis_run_id")
    private java.util.UUID analysisRunId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AnalysisRun.RunStatus status = AnalysisRun.RunStatus.PENDING;

    @Column(name = "total_suggestions")
    private int totalSuggestions;

    @Column(name = "applied_suggestions")
    private int appliedSuggestions;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "llm_metadata", columnDefinition = "jsonb")
    private Map<String, Object> llmMetadata;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @OneToMany(mappedBy = "modernizationRun", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ModernizationSuggestion> suggestions = new ArrayList<>();

    // ============================================================
    // Getters & Setters
    // ============================================================

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public java.util.UUID getAnalysisRunId() { return analysisRunId; }
    public void setAnalysisRunId(java.util.UUID analysisRunId) { this.analysisRunId = analysisRunId; }

    public AnalysisRun.RunStatus getStatus() { return status; }
    public void setStatus(AnalysisRun.RunStatus status) { this.status = status; }

    public int getTotalSuggestions() { return totalSuggestions; }
    public void setTotalSuggestions(int totalSuggestions) { this.totalSuggestions = totalSuggestions; }

    public int getAppliedSuggestions() { return appliedSuggestions; }
    public void setAppliedSuggestions(int appliedSuggestions) { this.appliedSuggestions = appliedSuggestions; }

    public Map<String, Object> getLlmMetadata() { return llmMetadata; }
    public void setLlmMetadata(Map<String, Object> llmMetadata) { this.llmMetadata = llmMetadata; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public List<ModernizationSuggestion> getSuggestions() { return suggestions; }
    public void setSuggestions(List<ModernizationSuggestion> suggestions) { this.suggestions = suggestions; }
}
