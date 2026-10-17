package com.sanket.AI.Code.Review.Platform.review.sonar.controller;

import com.sanket.AI.Code.Review.Platform.review.sonar.dto.SonarConnectionStatusResponse;
import com.sanket.AI.Code.Review.Platform.review.sonar.dto.SonarConnectionTestRequest;
import com.sanket.AI.Code.Review.Platform.review.sonar.service.SonarQubeConnectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sonarqube")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "SonarQube Integration", description = "Endpoints for inspecting and testing SonarQube connectivity")
@SecurityRequirement(name = "bearerAuth")
public class SonarQubeController {

    private final SonarQubeConnectionService sonarQubeConnectionService;

    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'TEAM_LEAD', 'ADMIN')")
    @Operation(summary = "Get SonarQube Connection Status", description = "Returns health status, version, and project availability of configured SonarQube server")
    public ResponseEntity<SonarConnectionStatusResponse> getStatus() {
        SonarConnectionStatusResponse response = sonarQubeConnectionService.checkConfiguredConnection();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/test-connection")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'TEAM_LEAD', 'ADMIN')")
    @Operation(summary = "Test SonarQube Connection (GET)", description = "Tests connection to SonarQube with optional URL, token, and project key query params")
    public ResponseEntity<SonarConnectionStatusResponse> testConnectionWithParams(
            @RequestParam(required = false) String sonarUrl,
            @RequestParam(required = false) String token,
            @RequestParam(required = false) String projectKey
    ) {
        SonarConnectionTestRequest request = SonarConnectionTestRequest.builder()
                .sonarUrl(sonarUrl)
                .token(token)
                .projectKey(projectKey)
                .build();
        return ResponseEntity.ok(sonarQubeConnectionService.testConnection(request));
    }

    @PostMapping("/test-connection")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEAM_LEAD')")
    @Operation(summary = "Test SonarQube Connection (POST)", description = "Tests connection using JSON body payload containing custom credentials")
    public ResponseEntity<SonarConnectionStatusResponse> testConnectionWithBody(
            @RequestBody(required = false) SonarConnectionTestRequest request
    ) {
        return ResponseEntity.ok(sonarQubeConnectionService.testConnection(request));
    }
}
