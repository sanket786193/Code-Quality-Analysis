package com.sanket.AI.Code.Review.Platform.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiReviewJobRequest {

    private Long reviewJobId;

    private String jobReference;

    private Long projectId;

    @NotBlank(message = "Pull request title is required")
    private String pullRequestTitle;

    private String pullRequestDescription;

    private String branchName;

    private String targetBranch;

    @Builder.Default
    private List<String> changedFiles = new ArrayList<>();

    @NotBlank(message = "Git diff content is required for AI review")
    private String gitDiff;

    @Builder.Default
    private List<String> ruleViolations = new ArrayList<>();

    private String staticAnalysisResults; // SonarQube quality gate or issue payload

    private String previousReviewContext;

    @Builder.Default
    private String programmingLanguage = "JAVA";
}
