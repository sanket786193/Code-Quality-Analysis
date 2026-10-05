package com.sanket.AI.Code.Review.Platform.project.mapper;

import com.sanket.AI.Code.Review.Platform.project.dto.response.ProjectResponse;
import com.sanket.AI.Code.Review.Platform.project.dto.response.RepositoryMappingResponse;
import com.sanket.AI.Code.Review.Platform.project.dto.response.UserSummaryDto;
import com.sanket.AI.Code.Review.Platform.project.entity.Project;
import com.sanket.AI.Code.Review.Platform.project.entity.RepositoryMapping;
import com.sanket.AI.Code.Review.Platform.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    public ProjectResponse toProjectResponse(Project project) {
        if (project == null) return null;

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus())
                .owner(toUserSummaryDto(project.getOwner()))
                .teamLead(toUserSummaryDto(project.getTeamLead()))
                .repositoryMapping(toRepositoryMappingResponse(project.getRepositoryMapping()))
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    public RepositoryMappingResponse toRepositoryMappingResponse(RepositoryMapping mapping) {
        if (mapping == null) return null;

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

    public UserSummaryDto toUserSummaryDto(User user) {
        if (user == null) return null;

        return UserSummaryDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }
}
