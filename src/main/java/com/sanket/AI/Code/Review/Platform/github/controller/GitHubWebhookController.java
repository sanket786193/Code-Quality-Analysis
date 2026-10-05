package com.sanket.AI.Code.Review.Platform.github.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanket.AI.Code.Review.Platform.exception.InvalidWebhookPayloadException;
import com.sanket.AI.Code.Review.Platform.github.dto.webhook.GitHubWebhookPayload;
import com.sanket.AI.Code.Review.Platform.github.service.GitHubIntegrationService;
import com.sanket.AI.Code.Review.Platform.github.webhook.GitHubWebhookHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "GitHub Webhooks", description = "Endpoints for ingesting GitHub App and repository events")
public class GitHubWebhookController {

    private final GitHubIntegrationService gitHubIntegrationService;
    private final GitHubWebhookHandler gitHubWebhookHandler;
    private final ObjectMapper objectMapper;

    @PostMapping("/github")
    @Operation(summary = "Receive GitHub Webhook Event", description = "Validates HMAC-SHA256 signature and enqueues event for async processing")
    public ResponseEntity<Map<String, Object>> handleGitHubWebhook(
            @RequestHeader(value = "X-GitHub-Event", defaultValue = "ping") String eventType,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature,
            @RequestHeader(value = "X-GitHub-Delivery", required = false) String deliveryId,
            @RequestBody String rawPayload
    ) {
        log.info("Received GitHub webhook event: '{}', deliveryId: {}", eventType, deliveryId);

        // Verify HMAC-SHA256 signature if signature header is provided
        if (signature != null && !signature.isBlank()) {
            boolean isValid = gitHubIntegrationService.verifyWebhookSignature(rawPayload, signature);
            if (!isValid) {
                log.warn("Rejected GitHub webhook with invalid HMAC signature");
                throw new InvalidWebhookPayloadException("Invalid GitHub webhook signature header.");
            }
        }

        try {
            GitHubWebhookPayload payload = objectMapper.readValue(rawPayload, GitHubWebhookPayload.class);
            // Process event asynchronously
            gitHubWebhookHandler.processWebhookEventAsync(eventType, payload);

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                    "status", "ACCEPTED",
                    "event", eventType,
                    "deliveryId", deliveryId != null ? deliveryId : "N/A",
                    "message", "Webhook event accepted for asynchronous review processing."
            ));
        } catch (Exception ex) {
            log.error("Failed to parse GitHub webhook payload: {}", ex.getMessage());
            throw new InvalidWebhookPayloadException("Malformed webhook JSON body: " + ex.getMessage());
        }
    }
}
