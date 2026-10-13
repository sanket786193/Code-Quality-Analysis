package com.sanket.AI.Code.Review.Platform.review.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateManualReviewRequest {

    @NotBlank(message = "Branch name is required")
    private String branchName;

    private String commitSha;

    private Integer pullRequestNumber;

    private String pullRequestTitle;
}
