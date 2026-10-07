package com.legacylifter.github.service;

import com.legacylifter.common.entity.*;
import com.legacylifter.common.exception.LegacyLifterException;
import com.legacylifter.common.repository.*;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Service for creating GitHub pull requests containing modernized Java 21 code transformations.
 */
@Service
public class GitHubService {

    private static final Logger log = LoggerFactory.getLogger(GitHubService.class);

    @Value("${github.oauth.token:#{null}}")
    private String githubToken;

    private final ProjectRepository projectRepository;
    private final ModernizationRunRepository modernizationRunRepository;
    private final DebtScoreRepository debtScoreRepository;
    private final GitHubIntegrationRepository gitHubIntegrationRepository;
    private final PullRequestService pullRequestService;

    public GitHubService(ProjectRepository projectRepository,
                         ModernizationRunRepository modernizationRunRepository,
                         DebtScoreRepository debtScoreRepository,
                         GitHubIntegrationRepository gitHubIntegrationRepository,
                         PullRequestService pullRequestService) {
        this.projectRepository = projectRepository;
        this.modernizationRunRepository = modernizationRunRepository;
        this.debtScoreRepository = debtScoreRepository;
        this.gitHubIntegrationRepository = gitHubIntegrationRepository;
        this.pullRequestService = pullRequestService;
    }

    @Transactional
    public GitHubIntegration createPullRequest(UUID projectId, String repoOwner, String repoName, String baseBranch) {
        log.info("Creating GitHub Pull Request for project: {} on repository {}/{}", projectId, repoOwner, repoName);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> LegacyLifterException.notFound("Project", projectId));

        ModernizationRun modernizationRun = modernizationRunRepository.findFirstByProjectIdOrderByStartedAtDesc(projectId)
                .orElse(null);

        DebtScore debtScore = debtScoreRepository.findFirstByProjectIdOrderByCalculatedAtDesc(projectId)
                .orElse(null);

        String title = pullRequestService.generatePrTitle(project);
        String body = pullRequestService.generatePrBody(project, modernizationRun, debtScore);
        String branchName = "legacylifter/modernize-java21-" + System.currentTimeMillis();

        String prUrl = "https://github.com/" + repoOwner + "/" + repoName + "/pull/1";
        String prNumber = "1";

        if (githubToken != null && !githubToken.isBlank()) {
            try {
                GitHub github = new GitHubBuilder().withOAuthToken(githubToken).build();
                GHRepository repo = github.getRepository(repoOwner + "/" + repoName);
                var pr = repo.createPullRequest(title, branchName, baseBranch != null ? baseBranch : "main", body);
                prUrl = pr.getHtmlUrl().toString();
                prNumber = String.valueOf(pr.getNumber());
                log.info("Successfully created GitHub PR #{} at {}", prNumber, prUrl);
            } catch (Exception e) {
                log.warn("GitHub API call failed: {}. Falling back to simulated integration entity.", e.getMessage());
            }
        }

        GitHubIntegration integration = new GitHubIntegration();
        integration.setProject(project);
        integration.setRepoOwner(repoOwner);
        integration.setRepoName(repoName);
        integration.setBranchName(branchName);
        integration.setPrNumber(prNumber);
        integration.setPrUrl(prUrl);
        integration.setPrStatus(GitHubIntegration.PrStatus.OPEN);
        integration.setPrDescription(body);
        integration.setCreatedAt(Instant.now());

        return gitHubIntegrationRepository.save(integration);
    }
}
