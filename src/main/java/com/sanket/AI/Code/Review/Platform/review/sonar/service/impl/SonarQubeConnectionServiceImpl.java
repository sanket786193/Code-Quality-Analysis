package com.sanket.AI.Code.Review.Platform.review.sonar.service.impl;

import com.sanket.AI.Code.Review.Platform.exception.SonarQubeConfigurationException;
import com.sanket.AI.Code.Review.Platform.exception.SonarQubeConnectionException;
import com.sanket.AI.Code.Review.Platform.review.sonar.client.SonarQubeClient;
import com.sanket.AI.Code.Review.Platform.review.sonar.dto.SonarConnectionStatus;
import com.sanket.AI.Code.Review.Platform.review.sonar.dto.SonarConnectionStatusResponse;
import com.sanket.AI.Code.Review.Platform.review.sonar.dto.SonarConnectionTestRequest;
import com.sanket.AI.Code.Review.Platform.review.sonar.service.SonarQubeConnectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SonarQubeConnectionServiceImpl implements SonarQubeConnectionService {

    private final SonarQubeClient sonarQubeClient;

    @Override
    public SonarConnectionStatusResponse checkConfiguredConnection() {
        return testConnection(SonarConnectionTestRequest.builder()
                .sonarUrl(sonarQubeClient.getConfiguredUrl())
                .token(sonarQubeClient.getConfiguredToken())
                .projectKey(sonarQubeClient.getDefaultProjectKey())
                .build());
    }

    @Override
    public SonarConnectionStatusResponse testConnection(SonarConnectionTestRequest request) {
        long startTime = System.currentTimeMillis();

        String targetUrl = (request != null && request.getSonarUrl() != null && !request.getSonarUrl().isBlank())
                ? request.getSonarUrl().trim()
                : sonarQubeClient.getConfiguredUrl();

        String token = (request != null && request.getToken() != null && !request.getToken().isBlank())
                ? request.getToken().trim()
                : sonarQubeClient.getConfiguredToken();

        String projectKey = (request != null && request.getProjectKey() != null && !request.getProjectKey().isBlank())
                ? request.getProjectKey().trim()
                : sonarQubeClient.getDefaultProjectKey();

        if (targetUrl == null || targetUrl.isBlank()) {
            throw new SonarQubeConfigurationException("SonarQube server URL is not configured.");
        }

        log.info("Testing SonarQube connection to '{}' for project '{}'", targetUrl, projectKey);

        SonarQubeClient.SonarSystemStatus systemStatus;
        try {
            // 1. Verify Server Availability
            systemStatus = sonarQubeClient.fetchSystemStatus(targetUrl);
        } catch (SonarQubeConnectionException ex) {
            long latency = System.currentTimeMillis() - startTime;
            log.warn("SonarQube connection test failed: {}", ex.getMessage());
            return SonarConnectionStatusResponse.builder()
                    .status(SonarConnectionStatus.SONARQUBE_UNAVAILABLE)
                    .message("SonarQube is unavailable or connection was refused: " + ex.getMessage())
                    .serverUrl(targetUrl)
                    .authenticated(false)
                    .projectKey(projectKey)
                    .projectExists(false)
                    .latencyMs(latency)
                    .timestamp(LocalDateTime.now())
                    .build();
        } catch (Exception ex) {
            long latency = System.currentTimeMillis() - startTime;
            return SonarConnectionStatusResponse.builder()
                    .status(SonarConnectionStatus.NOT_CONNECTED)
                    .message("SonarQube connection failed: " + ex.getMessage())
                    .serverUrl(targetUrl)
                    .authenticated(false)
                    .projectKey(projectKey)
                    .projectExists(false)
                    .latencyMs(latency)
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        String serverVersion = (systemStatus != null) ? systemStatus.getVersion() : "UNKNOWN";
        String serverState = (systemStatus != null) ? systemStatus.getStatus() : "DOWN";

        if (!"UP".equalsIgnoreCase(serverState)) {
            long latency = System.currentTimeMillis() - startTime;
            return SonarConnectionStatusResponse.builder()
                    .status(SonarConnectionStatus.NOT_CONNECTED)
                    .message("SonarQube server is not fully operational (state: " + serverState + ")")
                    .serverUrl(targetUrl)
                    .serverVersion(serverVersion)
                    .serverStatus(serverState)
                    .authenticated(false)
                    .projectKey(projectKey)
                    .projectExists(false)
                    .latencyMs(latency)
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        // 2. Verify Authentication Token
        boolean tokenValid = false;
        if (token != null && !token.isBlank()) {
            tokenValid = sonarQubeClient.validateAuthentication(targetUrl, token);
            if (!tokenValid) {
                long latency = System.currentTimeMillis() - startTime;
                return SonarConnectionStatusResponse.builder()
                        .status(SonarConnectionStatus.INVALID_TOKEN)
                        .message("SonarQube token is invalid or unauthorized")
                        .serverUrl(targetUrl)
                        .serverVersion(serverVersion)
                        .serverStatus(serverState)
                        .authenticated(false)
                        .projectKey(projectKey)
                        .projectExists(false)
                        .latencyMs(latency)
                        .timestamp(LocalDateTime.now())
                        .build();
            }
        }

        // 3. Verify Project Key Existence (if provided)
        boolean projectExists = false;
        if (projectKey != null && !projectKey.isBlank()) {
            projectExists = sonarQubeClient.checkProjectExists(targetUrl, token, projectKey);
            if (!projectExists) {
                long latency = System.currentTimeMillis() - startTime;
                return SonarConnectionStatusResponse.builder()
                        .status(SonarConnectionStatus.PROJECT_NOT_FOUND)
                        .message("SonarQube project key '" + projectKey + "' not found on server")
                        .serverUrl(targetUrl)
                        .serverVersion(serverVersion)
                        .serverStatus(serverState)
                        .authenticated(tokenValid)
                        .projectKey(projectKey)
                        .projectExists(false)
                        .latencyMs(latency)
                        .timestamp(LocalDateTime.now())
                        .build();
            }
        }

        long latency = System.currentTimeMillis() - startTime;
        log.info("SonarQube connection verified successfully (version={}, latency={}ms)", serverVersion, latency);

        return SonarConnectionStatusResponse.builder()
                .status(SonarConnectionStatus.CONNECTED)
                .message("SonarQube is connected successfully")
                .serverUrl(targetUrl)
                .serverVersion(serverVersion)
                .serverStatus(serverState)
                .authenticated(tokenValid)
                .projectKey(projectKey)
                .projectExists(projectExists)
                .latencyMs(latency)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
