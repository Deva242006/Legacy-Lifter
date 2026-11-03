package com.legacylifter.common.controller;

import com.legacylifter.common.dto.ApiResponse;
import com.legacylifter.common.entity.CodeIssue;
import com.legacylifter.common.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard API", description = "Aggregated statistics for the LegacyLifter dashboard overview")
public class DashboardController {

    private final ProjectRepository projectRepository;
    private final AnalysisRunRepository analysisRunRepository;
    private final CodeIssueRepository codeIssueRepository;
    private final ModernizationSuggestionRepository modernizationSuggestionRepository;
    private final GitHubIntegrationRepository gitHubIntegrationRepository;
    private final DebtScoreRepository debtScoreRepository;

    public DashboardController(ProjectRepository projectRepository,
                               AnalysisRunRepository analysisRunRepository,
                               CodeIssueRepository codeIssueRepository,
                               ModernizationSuggestionRepository modernizationSuggestionRepository,
                               GitHubIntegrationRepository gitHubIntegrationRepository,
                               DebtScoreRepository debtScoreRepository) {
        this.projectRepository = projectRepository;
        this.analysisRunRepository = analysisRunRepository;
        this.codeIssueRepository = codeIssueRepository;
        this.modernizationSuggestionRepository = modernizationSuggestionRepository;
        this.gitHubIntegrationRepository = gitHubIntegrationRepository;
        this.debtScoreRepository = debtScoreRepository;
    }

    @GetMapping("/overview")
    @Operation(summary = "Get aggregated dashboard statistics")
    public ApiResponse<Map<String, Object>> getOverview() {
        long totalProjects = projectRepository.count();
        long totalAnalysisRuns = analysisRunRepository.count();
        long totalIssuesFound = codeIssueRepository.count();
        long totalCriticalIssues = codeIssueRepository.countBySeverity(CodeIssue.IssueSeverity.CRITICAL);
        long totalSuggestionsApplied = modernizationSuggestionRepository.countByAppliedTrue();
        long totalPrsCreated = gitHubIntegrationRepository.count();

        double averageDebtScore = debtScoreRepository.findAverageOverallScore().orElse(0.0);

        return ApiResponse.success(Map.of(
                "totalProjects", totalProjects,
                "totalAnalysisRuns", totalAnalysisRuns,
                "totalIssuesFound", totalIssuesFound,
                "totalCriticalIssues", totalCriticalIssues,
                "totalSuggestionsApplied", totalSuggestionsApplied,
                "totalPrsCreated", totalPrsCreated,
                "averageDebtScore", Math.round(averageDebtScore * 10.0) / 10.0
        ));
    }
}
