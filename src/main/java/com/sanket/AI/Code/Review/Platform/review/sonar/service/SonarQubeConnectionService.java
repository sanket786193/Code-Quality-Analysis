package com.sanket.AI.Code.Review.Platform.review.sonar.service;

import com.sanket.AI.Code.Review.Platform.review.sonar.dto.SonarConnectionStatusResponse;
import com.sanket.AI.Code.Review.Platform.review.sonar.dto.SonarConnectionTestRequest;

public interface SonarQubeConnectionService {

    /**
     * Checks health and connection status using default configured SonarQube settings.
     */
    SonarConnectionStatusResponse checkConfiguredConnection();

    /**
     * Tests connectivity with optional custom server URL, token, and project key.
     */
    SonarConnectionStatusResponse testConnection(SonarConnectionTestRequest request);
}
