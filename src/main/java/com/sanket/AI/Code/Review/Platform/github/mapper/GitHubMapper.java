package com.sanket.AI.Code.Review.Platform.github.mapper;

import com.sanket.AI.Code.Review.Platform.github.dto.GitHubInstallationResponse;
import com.sanket.AI.Code.Review.Platform.github.model.GitHubInstallation;
import com.sanket.AI.Code.Review.Platform.project.dto.response.RepositoryMappingResponse;
import com.sanket.AI.Code.Review.Platform.project.entity.RepositoryMapping;
import org.springframework.stereotype.Component;

@Component
public class GitHubMapper {

    public GitHubInstallationResponse toInstallationResponse(GitHubInstallation installation) {
        if (installation == null) {
            return null;
        }

        return GitHubInstallationResponse.builder()
                .id(installation.getId())
                .installationId(installation.getInstallationId())
                .accountLogin(installation.getAccountLogin())
                .accountType(installation.getAccountType())
                .repositorySelection(installation.getRepositorySelection())
                .status(installation.getStatus())
                .userId(installation.getUser() != null ? installation.getUser().getId() : null)
                .installedAt(installation.getInstalledAt())
                .updatedAt(installation.getUpdatedAt())
                .build();
    }

    public RepositoryMappingResponse toRepositoryMappingResponse(RepositoryMapping mapping) {
        if (mapping == null) {
            return null;
        }

        return RepositoryMappingResponse.builder()
                .id(mapping.getId())
                .githubRepoId(mapping.getGithubRepoId())
                .installationId(mapping.getInstallation() != null ? mapping.getInstallation().getInstallationId() : null)
                .repoName(mapping.getRepoName())
                .repoFullName(mapping.getRepoFullName())
                .repoUrl(mapping.getRepoUrl())
                .defaultBranch(mapping.getDefaultBranch())
                .isPrivate(mapping.getIsPrivate())
                .webhookActive(mapping.getWebhookActive())
                .connectedAt(mapping.getConnectedAt())
                .build();
    }
}
