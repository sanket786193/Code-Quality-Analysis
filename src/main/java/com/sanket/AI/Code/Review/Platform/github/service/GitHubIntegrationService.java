package com.sanket.AI.Code.Review.Platform.github.service;

import com.sanket.AI.Code.Review.Platform.github.dto.GitHubInstallUrlResponse;
import com.sanket.AI.Code.Review.Platform.github.dto.GitHubInstallationResponse;
import com.sanket.AI.Code.Review.Platform.github.dto.GitHubRepositoryDto;
import com.sanket.AI.Code.Review.Platform.github.dto.MapRepositoryRequest;
import com.sanket.AI.Code.Review.Platform.github.dto.webhook.GitHubWebhookPayload;
import com.sanket.AI.Code.Review.Platform.github.model.GitHubInstallation;
import com.sanket.AI.Code.Review.Platform.project.dto.response.RepositoryMappingResponse;

import java.util.List;

public interface GitHubIntegrationService {

    /**
     * Generates a GitHub App installation URL with encoded user state.
     */
    GitHubInstallUrlResponse generateInstallUrl(Long userId);

    /**
     * Handles GitHub App post-install callback or redirect.
     */
    GitHubInstallationResponse handleInstallationCallback(Long installationId, String setupAction, Long userId);

    /**
     * Handles installation and installation_repositories webhooks dispatched from GitHub.
     */
    GitHubInstallation handleInstallationWebhook(GitHubWebhookPayload payload);

    /**
     * Fetches repositories accessible to the specified GitHub App installation.
     */
    List<GitHubRepositoryDto> fetchInstallationRepositories(Long installationId, Long userId);

    /**
     * Retrieves all active GitHub installations owned by or accessible to the user.
     */
    List<GitHubInstallationResponse> getUserInstallations(Long userId);

    /**
     * Maps a GitHub repository to a project.
     */
    RepositoryMappingResponse mapRepositoryToProject(MapRepositoryRequest request, Long userId);

    /**
     * Unmaps/disconnects the repository mapped to a project.
     */
    void unmapRepository(Long projectId, Long userId);

    /**
     * Retrieves repository mapping for a project.
     */
    RepositoryMappingResponse getRepositoryMappingByProject(Long projectId, Long userId);

    /**
     * Validates GitHub webhook HMAC SHA-256 signature against the configured webhook secret.
     */
    boolean verifyWebhookSignature(String payload, String signatureHeader);
}
