package com.legacylifter.github.service;

import com.legacylifter.analysis.service.AnalysisOrchestrator;
import com.legacylifter.common.entity.Project;
import com.legacylifter.common.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;

/**
 * Processes incoming GitHub webhook events.
 *
 * <p>Currently handles:
 * <ul>
 *   <li>{@code push} – auto-triggers a new analysis on the affected project (if connected)</li>
 *   <li>{@code pull_request} – logs PR lifecycle events for audit purposes</li>
 *   <li>{@code ping} – acknowledges the webhook setup</li>
 * </ul>
 * </p>
 */
@Service
public class WebhookProcessor {

    private static final Logger log = LoggerFactory.getLogger(WebhookProcessor.class);

    @Value("${legacylifter.github.webhook-secret:}")
    private String webhookSecret;

    private final ProjectRepository projectRepository;
    private final AnalysisOrchestrator analysisOrchestrator;

    public WebhookProcessor(ProjectRepository projectRepository,
                            AnalysisOrchestrator analysisOrchestrator) {
        this.projectRepository = projectRepository;
        this.analysisOrchestrator = analysisOrchestrator;
    }

    /**
     * Dispatch a webhook event to the appropriate handler.
     *
     * @param event     GitHub event type (from X-GitHub-Event header)
     * @param signature HMAC-SHA256 signature (from X-Hub-Signature-256 header), may be null
     * @param payload   deserialized JSON body
     */
    public void process(String event, String signature, Map<String, Object> payload) {
        log.debug("Processing webhook event '{}' with payload keys: {}", event, payload.keySet());

        switch (event) {
            case "push"         -> handlePush(payload);
            case "pull_request" -> handlePullRequest(payload);
            case "ping"         -> log.info("GitHub webhook ping received — all good!");
            default             -> log.debug("Ignoring unhandled webhook event: {}", event);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Event handlers
    // ─────────────────────────────────────────────────────────────────────────

    @Async
    @SuppressWarnings("unchecked")
    protected void handlePush(Map<String, Object> payload) {
        try {
            // Extract repo full_name (e.g. "owner/repo")
            Map<String, Object> repo = (Map<String, Object>) payload.get("repository");
            if (repo == null) return;

            String fullName = (String) repo.get("full_name");
            String ref      = (String) payload.getOrDefault("ref", "");

            // Only react to pushes to the default / main branch
            if (!ref.endsWith("/main") && !ref.endsWith("/master")) {
                log.debug("Ignoring push to non-default branch: {}", ref);
                return;
            }

            String htmlUrl = (String) repo.get("html_url");
            log.info("Handling push event for repo '{}' on ref '{}'", fullName, ref);

            // Find a project connected to this repository URL
            Optional<Project> projectOpt = projectRepository
                    .findAll()
                    .stream()
                    .filter(p -> htmlUrl != null && htmlUrl.equalsIgnoreCase(p.getGithubRepoUrl()))
                    .findFirst();

            projectOpt.ifPresentOrElse(
                project -> {
                    log.info("Auto-triggering analysis for project '{}' due to push event", project.getId());
                    analysisOrchestrator.runAnalysis(project.getId(), null);
                },
                () -> log.debug("No LegacyLifter project connected to repo '{}'", fullName)
            );
        } catch (Exception e) {
            log.error("Error handling push webhook: {}", e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    protected void handlePullRequest(Map<String, Object> payload) {
        String action = (String) payload.getOrDefault("action", "unknown");
        Map<String, Object> pr = (Map<String, Object>) payload.get("pull_request");
        if (pr != null) {
            Object number = pr.get("number");
            String title  = (String) pr.getOrDefault("title", "");
            log.info("Pull request #{} action='{}' title='{}'", number, action, title);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Signature verification (optional)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Verifies the X-Hub-Signature-256 header.
     * Returns {@code true} if no secret is configured (open endpoint in dev).
     */
    public boolean verifySignature(String signature, byte[] body) {
        if (webhookSecret == null || webhookSecret.isBlank()) return true;
        if (signature == null || !signature.startsWith("sha256=")) return false;

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] expected = mac.doFinal(body);
            String expectedHex = "sha256=" + HexFormat.of().formatHex(expected);
            return expectedHex.equalsIgnoreCase(signature);
        } catch (Exception e) {
            log.error("Signature verification failed: {}", e.getMessage());
            return false;
        }
    }
}
