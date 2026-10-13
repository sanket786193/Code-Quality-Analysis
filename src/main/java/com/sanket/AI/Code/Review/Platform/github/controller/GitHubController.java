package com.sanket.AI.Code.Review.Platform.github.controller;

import com.sanket.AI.Code.Review.Platform.github.dto.GitHubInstallUrlResponse;
import com.sanket.AI.Code.Review.Platform.github.dto.GitHubInstallationResponse;
import com.sanket.AI.Code.Review.Platform.github.dto.GitHubRepositoryDto;
import com.sanket.AI.Code.Review.Platform.github.dto.MapRepositoryRequest;
import com.sanket.AI.Code.Review.Platform.github.service.GitHubIntegrationService;
import com.sanket.AI.Code.Review.Platform.project.dto.response.RepositoryMappingResponse;
import com.sanket.AI.Code.Review.Platform.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "GitHub Integration", description = "Endpoints for managing GitHub App installations and repository mappings")
@SecurityRequirement(name = "bearerAuth")
public class GitHubController {

    private final GitHubIntegrationService gitHubIntegrationService;

    @GetMapping("/install-url")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get GitHub App Install URL", description = "Generates the GitHub App installation URL with user state token")
    public ResponseEntity<GitHubInstallUrlResponse> getInstallUrl(@AuthenticationPrincipal UserPrincipal currentUser) {
        GitHubInstallUrlResponse response = gitHubIntegrationService.generateInstallUrl(currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/callback")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "GitHub Installation Callback", description = "Handles the redirect callback after a user installs the GitHub App")
    public ResponseEntity<GitHubInstallationResponse> handleInstallationCallback(
            @RequestParam("installation_id") Long installationId,
            @RequestParam(value = "setup_action", required = false) String setupAction,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        GitHubInstallationResponse response = gitHubIntegrationService.handleInstallationCallback(
                installationId, setupAction, currentUser.getId()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/installations")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get User GitHub Installations", description = "Lists all GitHub App installations linked to the current user")
    public ResponseEntity<List<GitHubInstallationResponse>> getUserInstallations(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        List<GitHubInstallationResponse> response = gitHubIntegrationService.getUserInstallations(currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/installations/{installationId}/repositories")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Fetch Accessible Repositories", description = "Fetches repositories granted to the specified GitHub App installation")
    public ResponseEntity<List<GitHubRepositoryDto>> getInstallationRepositories(
            @PathVariable Long installationId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        List<GitHubRepositoryDto> repositories = gitHubIntegrationService.fetchInstallationRepositories(
                installationId, currentUser.getId()
        );
        return ResponseEntity.ok(repositories);
    }

    @PostMapping("/repositories/map")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN')")
    @Operation(summary = "Map Repository to Project", description = "Binds an installed GitHub repository to a project")
    public ResponseEntity<RepositoryMappingResponse> mapRepositoryToProject(
            @Valid @RequestBody MapRepositoryRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        RepositoryMappingResponse response = gitHubIntegrationService.mapRepositoryToProject(
                request, currentUser.getId()
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/projects/{projectId}/repository")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'TEAM_LEAD', 'ADMIN')")
    @Operation(summary = "Get Project Repository Mapping", description = "Retrieves the GitHub repository connected to a specific project")
    public ResponseEntity<RepositoryMappingResponse> getProjectRepository(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        RepositoryMappingResponse response = gitHubIntegrationService.getRepositoryMappingByProject(
                projectId, currentUser.getId()
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/projects/{projectId}/repository")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN')")
    @Operation(summary = "Unmap Project Repository", description = "Disconnects the repository from the project")
    public ResponseEntity<Map<String, String>> unmapRepository(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        gitHubIntegrationService.unmapRepository(projectId, currentUser.getId());
        return ResponseEntity.ok(Map.of("message", "Repository successfully unmapped from project"));
    }
}
