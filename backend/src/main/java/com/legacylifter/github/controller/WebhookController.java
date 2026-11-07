package com.legacylifter.github.controller;

import com.legacylifter.common.dto.ApiResponse;
import com.legacylifter.github.service.WebhookProcessor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller receiving GitHub webhook events.
 *
 * <p>Handles {@code pull_request}, {@code push}, and {@code installation} events.
 * Validates HMAC SHA-256 signatures when a webhook secret is configured.</p>
 */
@RestController
@RequestMapping("/webhooks")
@Tag(name = "GitHub Webhooks", description = "Endpoints for inbound GitHub webhook delivery")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final WebhookProcessor webhookProcessor;

    public WebhookController(WebhookProcessor webhookProcessor) {
        this.webhookProcessor = webhookProcessor;
    }

    /**
     * Inbound webhook endpoint.
     *
     * @param event     the GitHub event name (from {@code X-GitHub-Event} header)
     * @param signature HMAC SHA-256 signature (from {@code X-Hub-Signature-256} header)
     * @param payload   the raw JSON body sent by GitHub
     */
    @PostMapping("/github")
    @Operation(summary = "Receive GitHub webhook events")
    public ResponseEntity<ApiResponse<String>> receiveWebhook(
            @RequestHeader(value = "X-GitHub-Event", defaultValue = "unknown") String event,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature,
            @RequestBody Map<String, Object> payload) {

        log.info("Received GitHub webhook event: {}", event);

        try {
            webhookProcessor.process(event, signature, payload);
            return ResponseEntity.ok(ApiResponse.success("accepted"));
        } catch (Exception e) {
            log.error("Error processing GitHub webhook event '{}': {}", event, e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.success("Webhook received but processing failed: " + e.getMessage()));
        }
    }

    /** Simple ping endpoint so GitHub can verify the webhook URL. */
    @GetMapping("/github/ping")
    @Operation(summary = "Webhook ping verification")
    public ResponseEntity<ApiResponse<String>> ping() {
        return ResponseEntity.ok(ApiResponse.success("pong"));
    }
}
