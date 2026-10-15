package com.legacylifter.github.controller;

import com.legacylifter.common.dto.ApiResponse;
import com.legacylifter.github.service.WebhookProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Receives GitHub webhook payloads.
 *
 * <p>Supports {@code push} and {@code pull_request} event types.
 * Validates the {@code X-Hub-Signature-256} header when a webhook secret is configured.</p>
 */
@RestController
@RequestMapping("/api/v1/webhooks")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final WebhookProcessor webhookProcessor;

    public WebhookController(WebhookProcessor webhookProcessor) {
        this.webhookProcessor = webhookProcessor;
    }

    /**
     * Main webhook endpoint called by GitHub.
     *
     * @param event     the {@code X-GitHub-Event} header value (e.g. "push", "pull_request")
     * @param signature the {@code X-Hub-Signature-256} header value (HMAC-SHA256 of the body)
     * @param payload   the raw JSON body sent by GitHub
     */
    @PostMapping("/github")
    public ResponseEntity<ApiResponse<String>> receiveWebhook(
            @RequestHeader(value = "X-GitHub-Event", defaultValue = "unknown") String event,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature,
            @RequestBody Map<String, Object> payload) {

        log.info("Received GitHub webhook event: {}", event);

        try {
            webhookProcessor.process(event, signature, payload);
            return ResponseEntity.ok(ApiResponse.success("Webhook processed", "accepted"));
        } catch (Exception e) {
            log.error("Error processing GitHub webhook event '{}': {}", event, e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.success("Webhook received but processing failed", "error"));
        }
    }

    /** Simple ping endpoint so GitHub can verify the webhook URL. */
    @GetMapping("/github/ping")
    public ResponseEntity<ApiResponse<String>> ping() {
        return ResponseEntity.ok(ApiResponse.success("LegacyLifter webhook endpoint is alive", "pong"));
    }
}
