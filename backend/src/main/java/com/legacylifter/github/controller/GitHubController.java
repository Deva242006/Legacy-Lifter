package com.legacylifter.github.controller;

import com.legacylifter.common.dto.ApiResponse;
import com.legacylifter.common.entity.GitHubIntegration;
import com.legacylifter.common.repository.GitHubIntegrationRepository;
import com.legacylifter.github.dto.GitHubDtos.*;
import com.legacylifter.github.service.GitHubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@Tag(name = "GitHub Integration API", description = "Endpoints for creating automated Pull Requests on GitHub repositories")
public class GitHubController {

    private final GitHubService gitHubService;
    private final GitHubIntegrationRepository gitHubIntegrationRepository;

    public GitHubController(GitHubService gitHubService, GitHubIntegrationRepository gitHubIntegrationRepository) {
        this.gitHubService = gitHubService;
        this.gitHubIntegrationRepository = gitHubIntegrationRepository;
    }

    @PostMapping("/{id}/github/pull-request")
    @Operation(summary = "Create an automated GitHub Pull Request with modernized Java code")
    public ResponseEntity<ApiResponse<GitHubIntegrationResponse>> createPullRequest(
            @PathVariable UUID id,
            @RequestBody CreatePrRequest request) {

        GitHubIntegration integration = gitHubService.createPullRequest(
                id,
                request.repoOwner(),
                request.repoName(),
                request.baseBranch()
        );
        return ResponseEntity.ok(ApiResponse.success(GitHubIntegrationResponse.from(integration)));
    }

    @GetMapping("/{id}/github/integrations")
    @Operation(summary = "List created GitHub Pull Request integrations for project")
    public ResponseEntity<ApiResponse<List<GitHubIntegrationResponse>>> getIntegrations(@PathVariable UUID id) {
        List<GitHubIntegrationResponse> list = gitHubIntegrationRepository.findByProjectId(id)
                .stream()
                .map(GitHubIntegrationResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(list));
    }
}
