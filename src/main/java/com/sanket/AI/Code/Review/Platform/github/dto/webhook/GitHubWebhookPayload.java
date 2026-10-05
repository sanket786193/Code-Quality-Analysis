package com.sanket.AI.Code.Review.Platform.github.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubWebhookPayload {

    private String action;

    private InstallationDto installation;

    private RepositoryDto repository;

    private SenderDto sender;

    @JsonProperty("pull_request")
    private PullRequestDto pullRequest;

    private String after; // Latest commit SHA on push

    private String ref; // Branch ref (e.g. refs/heads/feature-1)

    @JsonProperty("head_commit")
    private CommitDto headCommit;

    private List<RepositoryDto> repositories; // Repositories added during installation

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InstallationDto {
        private Long id;
        private AccountDto account;

        @JsonProperty("repository_selection")
        private String repositorySelection;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AccountDto {
        private String login;
        private String type;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RepositoryDto {
        private Long id;
        private String name;

        @JsonProperty("full_name")
        private String fullName;

        @JsonProperty("html_url")
        private String htmlUrl;

        @JsonProperty("default_branch")
        private String defaultBranch;

        @JsonProperty("private")
        private Boolean isPrivate;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SenderDto {
        private String login;
        private Long id;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PullRequestDto {
        private Integer number;
        private String title;
        private String state;
        private HeadBaseDto head;
        private HeadBaseDto base;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HeadBaseDto {
        private String sha;
        private String ref;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CommitDto {
        private String id;
        private String message;
    }
}
