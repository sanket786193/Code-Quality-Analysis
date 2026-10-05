package com.sanket.AI.Code.Review.Platform.github.webhook;

import com.sanket.AI.Code.Review.Platform.github.dto.webhook.GitHubWebhookPayload;
import com.sanket.AI.Code.Review.Platform.github.service.GitHubIntegrationService;
import com.sanket.AI.Code.Review.Platform.project.entity.RepositoryMapping;
import com.sanket.AI.Code.Review.Platform.project.repository.RepositoryMappingRepository;
import com.sanket.AI.Code.Review.Platform.review.entity.ReviewEventType;
import com.sanket.AI.Code.Review.Platform.review.service.ReviewJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Asynchronous handler for GitHub Webhooks.
 * Validates repository bindings, filters events, and dispatches review jobs.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GitHubWebhookHandler {

    private final RepositoryMappingRepository repositoryMappingRepository;
    private final ReviewJobService reviewJobService;
    private final GitHubIntegrationService gitHubIntegrationService;

    @Async("webhookTaskExecutor")
    public void processWebhookEventAsync(String eventType, GitHubWebhookPayload payload) {
        log.info("Processing asynchronous GitHub webhook event: '{}'", eventType);

        if (payload == null) {
            log.warn("Null payload received for event: {}", eventType);
            return;
        }

        switch (eventType) {
            case "pull_request" -> handlePullRequestEvent(payload);
            case "push" -> handlePushEvent(payload);
            case "installation", "installation_repositories" -> handleInstallationEvent(payload);
            case "ping" -> log.info("Received GitHub ping event. Connection active.");
            default -> log.info("Unhandled GitHub webhook event type: {}", eventType);
        }
    }

    private void handlePullRequestEvent(GitHubWebhookPayload payload) {
        String action = payload.getAction();
        log.info("Handling pull_request webhook action: {}", action);

        // Process only actionable PR events
        if (!"opened".equalsIgnoreCase(action) &&
            !"synchronize".equalsIgnoreCase(action) &&
            !"reopened".equalsIgnoreCase(action)) {
            log.info("Skipping unhandled PR action: {}", action);
            return;
        }

        if (payload.getRepository() == null || payload.getPullRequest() == null) {
            log.warn("Malformed pull_request payload: repository or pull_request object missing");
            return;
        }

        Long githubRepoId = payload.getRepository().getId();
        Optional<RepositoryMapping> mappingOpt = repositoryMappingRepository.findByGithubRepoId(githubRepoId);

        if (mappingOpt.isEmpty()) {
            log.warn("Received PR webhook for repository ID {} ({}) which is not linked to any project. Ignoring.",
                    githubRepoId, payload.getRepository().getFullName());
            return;
        }

        RepositoryMapping mapping = mappingOpt.get();
        if (!Boolean.TRUE.equals(mapping.getWebhookActive())) {
            log.info("Webhooks are disabled for repository: {}", mapping.getRepoFullName());
            return;
        }

        GitHubWebhookPayload.PullRequestDto pr = payload.getPullRequest();
        String commitSha = pr.getHead() != null ? pr.getHead().getSha() : "unknown";
        String branch = pr.getHead() != null ? pr.getHead().getRef() : "main";
        String sender = payload.getSender() != null ? payload.getSender().getLogin() : "unknown";

        reviewJobService.createReviewJobFromWebhook(
                mapping,
                ReviewEventType.PULL_REQUEST,
                commitSha,
                pr.getNumber(),
                pr.getTitle(),
                branch,
                sender
        );
    }

    private void handlePushEvent(GitHubWebhookPayload payload) {
        if (payload.getRepository() == null) {
            log.warn("Malformed push payload: missing repository object");
            return;
        }

        Long githubRepoId = payload.getRepository().getId();
        Optional<RepositoryMapping> mappingOpt = repositoryMappingRepository.findByGithubRepoId(githubRepoId);

        if (mappingOpt.isEmpty()) {
            log.warn("Received push webhook for repository ID {} which is not linked to any project. Ignoring.",
                    githubRepoId);
            return;
        }

        RepositoryMapping mapping = mappingOpt.get();
        if (!Boolean.TRUE.equals(mapping.getWebhookActive())) {
            log.info("Webhooks are disabled for repository: {}", mapping.getRepoFullName());
            return;
        }

        String commitSha = payload.getHeadCommit() != null ? payload.getHeadCommit().getId() : payload.getAfter();
        String ref = payload.getRef();
        String branch = ref != null && ref.startsWith("refs/heads/") ? ref.substring(11) : ref;
        String sender = payload.getSender() != null ? payload.getSender().getLogin() : "unknown";

        reviewJobService.createReviewJobFromWebhook(
                mapping,
                ReviewEventType.PUSH,
                commitSha,
                null,
                null,
                branch,
                sender
        );
    }

    private void handleInstallationEvent(GitHubWebhookPayload payload) {
        try {
            gitHubIntegrationService.handleInstallationWebhook(payload);
        } catch (Exception ex) {
            log.error("Failed to process installation webhook: {}", ex.getMessage(), ex);
        }
    }
}
