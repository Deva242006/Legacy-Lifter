package com.legacylifter.common.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Java project uploaded for analysis and modernization.
 *
 * <p>A project is the top-level entity that ties together analysis runs,
 * modernization suggestions, debt scores, generated tests, and GitHub integrations.</p>
 */
@Entity
@Table(name = "projects")
public class Project extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "source_url")
    private String sourceUrl;

    @Column(name = "github_repo_url")
    private String githubRepoUrl;

    @Column(name = "java_version_detected")
    private String javaVersionDetected;

    @Column(name = "target_java_version")
    private String targetJavaVersion = "21";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProjectStatus status = ProjectStatus.CREATED;

    @Column(name = "source_path")
    private String sourcePath;

    @Column(name = "total_files")
    private Integer totalFiles;

    @Column(name = "total_lines")
    private Long totalLines;

    // ============================================================
    // Relationships
    // ============================================================

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("startedAt DESC")
    private List<AnalysisRun> analysisRuns = new ArrayList<>();

    // ============================================================
    // Enums
    // ============================================================

    public enum ProjectStatus {
        CREATED,
        UPLOADING,
        READY,
        ANALYZING,
        ANALYZED,
        MODERNIZING,
        MODERNIZED,
        ERROR
    }

    // ============================================================
    // Getters & Setters
    // ============================================================

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public String getGithubRepoUrl() { return githubRepoUrl; }
    public void setGithubRepoUrl(String githubRepoUrl) { this.githubRepoUrl = githubRepoUrl; }

    public String getJavaVersionDetected() { return javaVersionDetected; }
    public void setJavaVersionDetected(String javaVersionDetected) { this.javaVersionDetected = javaVersionDetected; }

    public String getTargetJavaVersion() { return targetJavaVersion; }
    public void setTargetJavaVersion(String targetJavaVersion) { this.targetJavaVersion = targetJavaVersion; }

    public ProjectStatus getStatus() { return status; }
    public void setStatus(ProjectStatus status) { this.status = status; }

    public String getSourcePath() { return sourcePath; }
    public void setSourcePath(String sourcePath) { this.sourcePath = sourcePath; }

    public Integer getTotalFiles() { return totalFiles; }
    public void setTotalFiles(Integer totalFiles) { this.totalFiles = totalFiles; }

    public Long getTotalLines() { return totalLines; }
    public void setTotalLines(Long totalLines) { this.totalLines = totalLines; }

    public List<AnalysisRun> getAnalysisRuns() { return analysisRuns; }
    public void setAnalysisRuns(List<AnalysisRun> analysisRuns) { this.analysisRuns = analysisRuns; }
}
