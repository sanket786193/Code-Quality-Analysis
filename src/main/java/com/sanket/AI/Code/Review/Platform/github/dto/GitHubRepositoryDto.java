package com.sanket.AI.Code.Review.Platform.github.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubRepositoryDto {
    private Long id;
    private String name;

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("html_url")
    private String htmlUrl;

    @JsonProperty("default_branch")
    @Builder.Default
    private String defaultBranch = "main";

    @JsonProperty("private")
    @Builder.Default
    private Boolean isPrivate = false;

    private String description;
}
