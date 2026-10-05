package com.sanket.AI.Code.Review.Platform.github.service;

import com.sanket.AI.Code.Review.Platform.exception.GitHubInstallationException;
import com.sanket.AI.Code.Review.Platform.exception.InvalidWebhookPayloadException;
import com.sanket.AI.Code.Review.Platform.exception.ResourceNotFoundException;
import com.sanket.AI.Code.Review.Platform.github.client.GitHubApiClient;
import com.sanket.AI.Code.Review.Platform.github.dto.GitHubInstallUrlResponse;
import com.sanket.AI.Code.Review.Platform.github.dto.GitHubRepositoryDto;
import com.sanket.AI.Code.Review.Platform.github.dto.webhook.GitHubWebhookPayload;
import com.sanket.AI.Code.Review.Platform.github.model.GitHubInstallation;
import com.sanket.AI.Code.Review.Platform.github.repository.GitHubInstallationRepository;
import com.sanket.AI.Code.Review.Platform.user.entity.User;
import com.sanket.AI.Code.Review.Platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubIntegrationServiceImpl implements GitHubIntegrationService {

    private final GitHubInstallationRepository installationRepository;
    private final UserRepository userRepository;
    private final GitHubApiClient gitHubApiClient;

    @Value("${app.github.app-name:ai-code-review-assistant}")
    private String githubAppName;

    @Value("${app.github.webhook-secret:dev_webhook_secret_key}")
    private String webhookSecret;

    @Override
    public GitHubInstallUrlResponse generateInstallUrl(Long userId) {
        String stateToken = Base64.getUrlEncoder().withoutPadding().encodeToString(
                (userId + ":" + UUID.randomUUID()).getBytes(StandardCharsets.UTF_8)
        );

        String installUrl = String.format("https://github.com/apps/%s/installations/new?state=%s",
                githubAppName, stateToken);

        log.info("Generated GitHub App install URL for user ID {}: {}", userId, installUrl);

        return GitHubInstallUrlResponse.builder()
                .installUrl(installUrl)
                .appName(githubAppName)
                .state(stateToken)
                .build();
    }

    @Override
    @Transactional
    public GitHubInstallation handleInstallationCallback(Long installationId, String setupAction, Long userId) {
        if (installationId == null) {
            throw new GitHubInstallationException("Invalid installation callback: missing installation_id");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        GitHubInstallation installation = installationRepository.findByInstallationId(installationId)
                .orElseGet(() -> GitHubInstallation.builder()
                        .installationId(installationId)
                        .accountLogin(user.getUsername())
                        .accountType("User")
                        .repositorySelection("SELECTED")
                        .user(user)
                        .status("ACTIVE")
                        .installedAt(LocalDateTime.now())
                        .build());

        installation.setStatus("ACTIVE");
        installation.setUser(user);
        installation.setUpdatedAt(LocalDateTime.now());

        GitHubInstallation saved = installationRepository.save(installation);
        log.info("Saved GitHub installation details for installationId: {}, userId: {}", installationId, userId);
        return saved;
    }

    @Override
    @Transactional
    public GitHubInstallation handleInstallationWebhook(GitHubWebhookPayload payload) {
        if (payload == null || payload.getInstallation() == null) {
            throw new InvalidWebhookPayloadException("Missing installation payload in GitHub webhook");
        }

        Long installationId = payload.getInstallation().getId();
        String action = payload.getAction() != null ? payload.getAction() : "created";
        String accountLogin = payload.getInstallation().getAccount() != null
                ? payload.getInstallation().getAccount().getLogin() : "unknown";
        String accountType = payload.getInstallation().getAccount() != null
                ? payload.getInstallation().getAccount().getType() : "User";
        String repoSelection = payload.getInstallation().getRepositorySelection() != null
                ? payload.getInstallation().getRepositorySelection() : "SELECTED";

        GitHubInstallation installation = installationRepository.findByInstallationId(installationId)
                .orElse(null);

        if ("deleted".equalsIgnoreCase(action)) {
            if (installation != null) {
                installation.setStatus("DELETED");
                installation.setUpdatedAt(LocalDateTime.now());
                log.info("Marked GitHub installation {} as DELETED", installationId);
                return installationRepository.save(installation);
            }
            return null;
        }

        if (installation == null) {
            // If installation does not exist yet, find by sender or set to first admin user as fallback
            User user = userRepository.findByUsername(payload.getSender() != null ? payload.getSender().getLogin() : "")
                    .orElseGet(() -> userRepository.findAll().stream().findFirst().orElse(null));

            if (user == null) {
                throw new GitHubInstallationException("Cannot bind installation: No system user available");
            }

            installation = GitHubInstallation.builder()
                    .installationId(installationId)
                    .accountLogin(accountLogin)
                    .accountType(accountType)
                    .repositorySelection(repoSelection)
                    .user(user)
                    .status("ACTIVE")
                    .installedAt(LocalDateTime.now())
                    .build();
        } else {
            installation.setAccountLogin(accountLogin);
            installation.setAccountType(accountType);
            installation.setRepositorySelection(repoSelection);
            installation.setStatus("ACTIVE");
            installation.setUpdatedAt(LocalDateTime.now());
        }

        GitHubInstallation saved = installationRepository.save(installation);
        log.info("Processed installation webhook for installation ID: {}, status: {}", installationId, saved.getStatus());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GitHubRepositoryDto> fetchInstallationRepositories(Long installationId, Long userId) {
        GitHubInstallation installation = installationRepository.findByInstallationId(installationId)
                .orElseThrow(() -> new GitHubInstallationException("GitHub Installation not found with id: " + installationId));

        if (!installation.getUser().getId().equals(userId)) {
            log.warn("User {} attempted to access installation {} owned by user {}",
                    userId, installationId, installation.getUser().getId());
            // Proceed if authorized or admin, or verify ownership
        }

        return gitHubApiClient.getInstallationRepositories(installationId, null);
    }

    @Override
    public boolean verifyWebhookSignature(String payload, String signatureHeader) {
        if (signatureHeader == null || !signatureHeader.startsWith("sha256=")) {
            log.warn("Invalid signature header format: {}", signatureHeader);
            return false;
        }

        try {
            String expectedSignature = signatureHeader.substring(7); // Remove "sha256="
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);

            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String calculatedSignature = HexFormat.of().formatHex(digest);

            return MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    calculatedSignature.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception ex) {
            log.error("Failed to verify HMAC webhook signature: {}", ex.getMessage());
            return false;
        }
    }
}
