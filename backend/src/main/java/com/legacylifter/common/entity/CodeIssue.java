package com.legacylifter.common.entity;

import jakarta.persistence.*;

/**
 * Represents a single code issue detected during static analysis.
 *
 * <p>Each issue maps to a specific location in the source code and
 * includes the detected pattern, severity, and a suggested fix.</p>
 */
@Entity
@Table(name = "code_issues", indexes = {
        @Index(name = "idx_code_issue_analysis_run", columnList = "analysis_run_id"),
        @Index(name = "idx_code_issue_category", columnList = "category"),
        @Index(name = "idx_code_issue_severity", columnList = "severity")
})
public class CodeIssue extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_run_id", nullable = false)
    private AnalysisRun analysisRun;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "line_number")
    private int lineNumber;

    @Column(name = "end_line_number")
    private int endLineNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private IssueCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private IssueSeverity severity;

    @Column(name = "pattern_type", nullable = false)
    private String patternType;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "original_code", columnDefinition = "TEXT")
    private String originalCode;

    @Column(name = "suggested_fix", columnDefinition = "TEXT")
    private String suggestedFix;

    @Column(name = "is_auto_fixable")
    private boolean autoFixable;

    // ============================================================
    // Enums
    // ============================================================

    public enum IssueCategory {
        RAW_TYPES,
        ANONYMOUS_CLASSES,
        OLD_CONCURRENCY,
        BLOCKING_IO,
        MISSING_RECORDS,
        MISSING_SEALED_CLASSES,
        OLD_SWITCH,
        STREAM_IMPROVEMENTS,
        OPTIONAL_MISUSE,
        SECURITY,
        PERFORMANCE,
        GENERAL
    }

    public enum IssueSeverity {
        CRITICAL,
        HIGH,
        MEDIUM,
        LOW,
        INFO
    }

    // ============================================================
    // Getters & Setters
    // ============================================================

    public AnalysisRun getAnalysisRun() { return analysisRun; }
    public void setAnalysisRun(AnalysisRun analysisRun) { this.analysisRun = analysisRun; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public int getLineNumber() { return lineNumber; }
    public void setLineNumber(int lineNumber) { this.lineNumber = lineNumber; }

    public int getEndLineNumber() { return endLineNumber; }
    public void setEndLineNumber(int endLineNumber) { this.endLineNumber = endLineNumber; }

    public IssueCategory getCategory() { return category; }
    public void setCategory(IssueCategory category) { this.category = category; }

    public IssueSeverity getSeverity() { return severity; }
    public void setSeverity(IssueSeverity severity) { this.severity = severity; }

    public String getPatternType() { return patternType; }
    public void setPatternType(String patternType) { this.patternType = patternType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getOriginalCode() { return originalCode; }
    public void setOriginalCode(String originalCode) { this.originalCode = originalCode; }

    public String getSuggestedFix() { return suggestedFix; }
    public void setSuggestedFix(String suggestedFix) { this.suggestedFix = suggestedFix; }

    public boolean isAutoFixable() { return autoFixable; }
    public void setAutoFixable(boolean autoFixable) { this.autoFixable = autoFixable; }
}
