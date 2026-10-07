package com.legacylifter.common.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a single static analysis execution on a project.
 *
 * <p>Each time the user triggers "Analyze", a new AnalysisRun is created.
 * It tracks progress (files analyzed) and aggregates detected issues.</p>
 */
@Entity
@Table(name = "analysis_runs")
public class AnalysisRun extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RunStatus status = RunStatus.PENDING;

    @Column(name = "total_files")
    private int totalFiles;

    @Column(name = "files_analyzed")
    private int filesAnalyzed;

    @Column(name = "issues_found")
    private int issuesFound;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "summary", columnDefinition = "jsonb")
    private Map<String, Object> summary;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "analysis_duration_ms")
    private Long analysisDurationMs;

    // ============================================================
    // Relationships
    // ============================================================

    @OneToMany(mappedBy = "analysisRun", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CodeIssue> issues = new ArrayList<>();

    // ============================================================
    // Enums
    // ============================================================

    public enum RunStatus {
        PENDING,
        RUNNING,
        COMPLETED,
        FAILED
    }

    // ============================================================
    // Getters & Setters
    // ============================================================

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public RunStatus getStatus() { return status; }
    public void setStatus(RunStatus status) { this.status = status; }

    public int getTotalFiles() { return totalFiles; }
    public void setTotalFiles(int totalFiles) { this.totalFiles = totalFiles; }

    public int getFilesAnalyzed() { return filesAnalyzed; }
    public void setFilesAnalyzed(int filesAnalyzed) { this.filesAnalyzed = filesAnalyzed; }

    public int getIssuesFound() { return issuesFound; }
    public void setIssuesFound(int issuesFound) { this.issuesFound = issuesFound; }

    public Map<String, Object> getSummary() { return summary; }
    public void setSummary(Map<String, Object> summary) { this.summary = summary; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public Long getAnalysisDurationMs() { return analysisDurationMs; }
    public void setAnalysisDurationMs(Long analysisDurationMs) { this.analysisDurationMs = analysisDurationMs; }

    public List<CodeIssue> getIssues() { return issues; }
    public void setIssues(List<CodeIssue> issues) { this.issues = issues; }
}
