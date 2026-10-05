package com.sanket.AI.Code.Review.Platform.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LinkRepositoryRequest {

    @NotNull(message = "GitHub installation ID is required")
    private Long installationId;

    @NotNull(message = "GitHub repository ID is required")
    private Long githubRepoId;

    @NotBlank(message = "Repository name is required")
    private String repoName;

    @NotBlank(message = "Repository full name (owner/repo) is required")
    private String repoFullName;

    @NotBlank(message = "Repository URL is required")
    private String repoUrl;

    @Builder.Default
    private String defaultBranch = "main";

    @Builder.Default
    private Boolean isPrivate = false;
}
