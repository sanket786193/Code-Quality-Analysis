package com.sanket.AI.Code.Review.Platform.review.sonar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Detailed report of SonarQube connection health check")
public class SonarConnectionStatusResponse {

    @Schema(description = "Overall connection status", example = "CONNECTED")
    private SonarConnectionStatus status;

    @Schema(description = "Human-readable status summary", example = "SonarQube is connected successfully")
    private String message;

    @Schema(description = "Target SonarQube server URL", example = "http://localhost:9000")
    private String serverUrl;

    @Schema(description = "SonarQube version detected", example = "10.4.1.88267")
    private String serverVersion;

    @Schema(description = "Server operational state", example = "UP")
    private String serverStatus;

    @Schema(description = "Whether the provided token is valid", example = "true")
    private Boolean authenticated;

    @Schema(description = "Configured or tested project key", example = "ai-code-review-platform")
    private String projectKey;

    @Schema(description = "Whether the project exists on SonarQube", example = "true")
    private Boolean projectExists;

    @Schema(description = "Round-trip ping latency in milliseconds", example = "42")
    private Long latencyMs;

    @Schema(description = "Timestamp when the health check was performed")
    private LocalDateTime timestamp;
}
