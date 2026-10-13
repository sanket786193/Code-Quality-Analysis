package com.sanket.AI.Code.Review.Platform.github.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GitHubInstallationResponse {

    private Long id;
    private Long installationId;
    private String accountLogin;
    private String accountType;
    private String repositorySelection;
    private String status;
    private Long userId;
    private LocalDateTime installedAt;
    private LocalDateTime updatedAt;
}
