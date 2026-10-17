package com.sanket.AI.Code.Review.Platform.github.service;

import com.sanket.AI.Code.Review.Platform.exception.*;
import com.sanket.AI.Code.Review.Platform.github.client.GitHubApiClient;
import com.sanket.AI.Code.Review.Platform.github.dto.GitHubInstallUrlResponse;
import com.sanket.AI.Code.Review.Platform.github.dto.GitHubInstallationResponse;
import com.sanket.AI.Code.Review.Platform.github.dto.GitHubRepositoryDto;
import com.sanket.AI.Code.Review.Platform.github.dto.MapRepositoryRequest;
import com.sanket.AI.Code.Review.Platform.github.dto.webhook.GitHubWebhookPayload;
import com.sanket.AI.Code.Review.Platform.github.mapper.GitHubMapper;
import com.sanket.AI.Code.Review.Platform.github.model.GitHubInstallation;
import com.sanket.AI.Code.Review.Platform.github.repository.GitHubInstallationRepository;
import com.sanket.AI.Code.Review.Platform.project.dto.response.RepositoryMappingResponse;
import com.sanket.AI.Code.Review.Platform.project.entity.Project;
import com.sanket.AI.Code.Review.Platform.project.entity.RepositoryMapping;
import com.sanket.AI.Code.Review.Platform.project.repository.ProjectRepository;
import com.sanket.AI.Code.Review.Platform.project.repository.RepositoryMappingRepository;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubIntegrationServiceImpl implements GitHubIntegrationService {

    private final GitHubInstallationRepository installationRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final RepositoryMappingRepository repositoryMappingRepository;
    private final GitHubApiClient gitHubApiClient;
    private final GitHubMapper gitHubMapper;

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
    public GitHubInstallationResponse handleInstallationCallback(Long installationId, String setupAction, Long userId) {
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
        return gitHubMapper.toInstallationResponse(saved);
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
            log.warn("User {} accessed installation {} owned by user {}",
                    userId, installationId, installation.getUser().getId());
        }

        return gitHubApiClient.getInstallationRepositories(installationId, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GitHubInstallationResponse> getUserInstallations(Long userId) {
        log.info("Fetching GitHub installations for user ID: {}", userId);
        return installationRepository.findByUserIdAndStatus(userId, "ACTIVE").stream()
                .map(gitHubMapper::toInstallationResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RepositoryMappingResponse mapRepositoryToProject(MapRepositoryRequest request, Long userId) {
        log.info("Mapping GitHub repo {} to project ID: {}", request.getRepoFullName(), request.getProjectId());

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + request.getProjectId()));

        // Validate that user is owner or has lead access
        if (project.getOwner() != null && !project.getOwner().getId().equals(userId)) {
            log.info("Project owner is user {}, request by user {}", project.getOwner().getId(), userId);
        }

        if (repositoryMappingRepository.existsByProjectId(request.getProjectId())) {
            throw new DuplicateRepositoryMappingException("Project '" + project.getName() + "' is already mapped to a GitHub repository.");
        }

        if (repositoryMappingRepository.existsByGithubRepoId(request.getGithubRepoId())) {
            throw new DuplicateRepositoryMappingException("GitHub repository '" + request.getRepoFullName() + "' is already mapped to another project.");
        }

        GitHubInstallation installation = installationRepository.findByInstallationId(request.getInstallationId())
                .orElseThrow(() -> new GitHubInstallationException("GitHub Installation not found with id: " + request.getInstallationId()));

        RepositoryMapping mapping = RepositoryMapping.builder()
                .project(project)
                .installation(installation)
                .githubRepoId(request.getGithubRepoId())
                .repoName(request.getRepoName())
                .repoFullName(request.getRepoFullName())
                .repoUrl(request.getRepoUrl())
                .defaultBranch(request.getDefaultBranch() != null ? request.getDefaultBranch() : "main")
                .isPrivate(request.getIsPrivate() != null ? request.getIsPrivate() : false)
                .webhookActive(true)
                .build();

        RepositoryMapping saved = repositoryMappingRepository.save(mapping);
        log.info("Successfully created repository mapping ID: {} for project: {}", saved.getId(), project.getName());
        return gitHubMapper.toRepositoryMappingResponse(saved);
    }

    @Override
    @Transactional
    public void unmapRepository(Long projectId, Long userId) {
        log.info("Unmapping repository for project ID: {}", projectId);
        RepositoryMapping mapping = repositoryMappingRepository.findByProjectId(projectId)
                .orElseThrow(() -> new RepositoryNotFoundException("No repository mapped to project with id: " + projectId));

        repositoryMappingRepository.delete(mapping);
        log.info("Unlinked repository mapping for project ID: {}", projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public RepositoryMappingResponse getRepositoryMappingByProject(Long projectId, Long userId) {
        RepositoryMapping mapping = repositoryMappingRepository.findByProjectId(projectId)
                .orElseThrow(() -> new RepositoryNotFoundException("No repository mapped to project with id: " + projectId));

        return gitHubMapper.toRepositoryMappingResponse(mapping);
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
