package com.legacylifter.analysis.dto;

import com.legacylifter.common.entity.AnalysisRun;
import com.legacylifter.common.entity.CodeIssue;
import com.legacylifter.common.entity.Project;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class AnalysisDtos {

    public record CreateProjectRequest(
            String name,
            String description,
            String sourceUrl,
            String githubRepoUrl,
            String sourceDirectoryPath,
            String targetJavaVersion
    ) {}

    public record ProjectResponse(
            UUID id,
            String name,
            String description,
            String sourceUrl,
            String githubRepoUrl,
            String javaVersionDetected,
            String targetJavaVersion,
            Project.ProjectStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static ProjectResponse from(Project p) {
            return new ProjectResponse(
                    p.getId(),
                    p.getName(),
                    p.getDescription(),
                    p.getSourceUrl(),
                    p.getGithubRepoUrl(),
                    p.getJavaVersionDetected(),
                    p.getTargetJavaVersion(),
                    p.getStatus(),
                    p.getCreatedAt(),
                    p.getUpdatedAt()
            );
        }
    }

    public record AnalysisRunResponse(
            UUID id,
            UUID projectId,
            AnalysisRun.RunStatus status,
            int totalFiles,
            int filesAnalyzed,
            int issuesFound,
            Map<String, Object> summary,
            Instant startedAt,
            Instant completedAt
    ) {
        public static AnalysisRunResponse from(AnalysisRun r) {
            return new AnalysisRunResponse(
                    r.getId(),
                    r.getProject().getId(),
                    r.getStatus(),
                    r.getTotalFiles(),
                    r.getFilesAnalyzed(),
                    r.getIssuesFound(),
                    r.getSummary(),
                    r.getStartedAt(),
                    r.getCompletedAt()
            );
        }
    }

    public record CodeIssueResponse(
            UUID id,
            UUID analysisRunId,
            String filePath,
            int lineNumber,
            int endLineNumber,
            CodeIssue.IssueCategory category,
            CodeIssue.IssueSeverity severity,
            String patternType,
            String description,
            String originalCode,
            String suggestedFix,
            boolean autoFixable
    ) {
        public static CodeIssueResponse from(CodeIssue i) {
            return new CodeIssueResponse(
                    i.getId(),
                    i.getAnalysisRun().getId(),
                    i.getFilePath(),
                    i.getLineNumber(),
                    i.getEndLineNumber(),
                    i.getCategory(),
                    i.getSeverity(),
                    i.getPatternType(),
                    i.getDescription(),
                    i.getOriginalCode(),
                    i.getSuggestedFix(),
                    i.isAutoFixable()
            );
        }
    }
}
