package com.legacylifter.github.dto;

import com.legacylifter.common.entity.GitHubIntegration;

import java.time.Instant;
import java.util.UUID;

public class GitHubDtos {

    public record CreatePrRequest(
            String repoOwner,
            String repoName,
            String baseBranch
    ) {}

    public record GitHubIntegrationResponse(
            UUID id,
            UUID projectId,
            String repoOwner,
            String repoName,
            String branchName,
            String prNumber,
            String prUrl,
            String prStatus,
            String prDescription,
            Instant createdAt
    ) {
        public static GitHubIntegrationResponse from(GitHubIntegration g) {
            return new GitHubIntegrationResponse(
                    g.getId(),
                    g.getProject().getId(),
                    g.getRepoOwner(),
                    g.getRepoName(),
                    g.getBranchName(),
                    g.getPrNumber(),
                    g.getPrUrl(),
                    g.getPrStatus() != null ? g.getPrStatus().name() : "OPEN",
                    g.getPrDescription(),
                    g.getCreatedAt()
            );
        }
    }
}
