package com.sanket.AI.Code.Review.Platform.review.sonar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to test custom or current SonarQube connection settings")
public class SonarConnectionTestRequest {

    @Schema(description = "Optional SonarQube host URL override", example = "http://localhost:9000")
    private String sonarUrl;

    @Schema(description = "Optional SonarQube User/Analysis Token override", example = "sqa_9876543210abcdef")
    private String token;

    @Schema(description = "Optional project key to verify", example = "ai-code-review-platform")
    private String projectKey;
}
