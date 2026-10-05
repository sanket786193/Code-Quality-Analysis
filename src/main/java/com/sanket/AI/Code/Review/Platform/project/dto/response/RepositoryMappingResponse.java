package com.sanket.AI.Code.Review.Platform.project.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepositoryMappingResponse {
    private Long id;
    private Long githubRepoId;
    private Long installationId;
    private String repoName;
    private String repoFullName;
    private String repoUrl;
    private String defaultBranch;
    private Boolean isPrivate;
    private Boolean webhookActive;
    private LocalDateTime connectedAt;
}
