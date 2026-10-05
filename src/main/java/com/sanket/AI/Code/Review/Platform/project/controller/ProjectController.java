package com.sanket.AI.Code.Review.Platform.project.controller;

import com.sanket.AI.Code.Review.Platform.github.dto.GitHubInstallUrlResponse;
import com.sanket.AI.Code.Review.Platform.github.dto.GitHubRepositoryDto;
import com.sanket.AI.Code.Review.Platform.github.service.GitHubIntegrationService;
import com.sanket.AI.Code.Review.Platform.project.dto.request.CreateProjectRequest;
import com.sanket.AI.Code.Review.Platform.project.dto.request.LinkRepositoryRequest;
import com.sanket.AI.Code.Review.Platform.project.dto.request.UpdateProjectRequest;
import com.sanket.AI.Code.Review.Platform.project.dto.response.ProjectDashboardResponse;
import com.sanket.AI.Code.Review.Platform.project.dto.response.ProjectResponse;
import com.sanket.AI.Code.Review.Platform.project.dto.response.RepositoryMappingResponse;
import com.sanket.AI.Code.Review.Platform.project.service.ProjectService;
import com.sanket.AI.Code.Review.Platform.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Project Management", description = "Endpoints for managing projects and GitHub repository integration")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private final ProjectService projectService;
    private final GitHubIntegrationService gitHubIntegrationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN')")
    @Operation(summary = "Create Project", description = "Creates a new code review project for the authenticated developer or admin")
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ProjectResponse response = projectService.createProject(request, currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DEVELOPER', 'TEAM_LEAD', 'ADMIN')")
    @Operation(summary = "List Projects", description = "Retrieves paginated projects accessible by user role")
    public ResponseEntity<Page<ProjectResponse>> getUserProjects(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ProjectResponse> page = projectService.getUserProjects(currentUser, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'TEAM_LEAD', 'ADMIN')")
    @Operation(summary = "Get Project by ID", description = "Fetches complete details for a single project")
    public ResponseEntity<ProjectResponse> getProjectById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ProjectResponse response = projectService.getProjectById(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN')")
    @Operation(summary = "Update Project", description = "Updates project details, status, or assigned team lead")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ProjectResponse response = projectService.updateProject(id, request, currentUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN')")
    @Operation(summary = "Delete Project", description = "Removes a project and associated repository mapping")
    public ResponseEntity<Map<String, String>> deleteProject(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        projectService.deleteProject(id, currentUser);
        return ResponseEntity.ok(Map.of("message", "Project deleted successfully"));
    }

    @PostMapping("/{id}/github/connect")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN')")
    @Operation(summary = "Connect GitHub App", description = "Generates the GitHub App installation URL to link repositories")
    public ResponseEntity<GitHubInstallUrlResponse> generateGitHubInstallUrl(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        // Validate project access before issuing install url
        projectService.getProjectById(id, currentUser);
        GitHubInstallUrlResponse response = gitHubIntegrationService.generateInstallUrl(currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/github/installations/{installationId}/repositories")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN')")
    @Operation(summary = "Fetch Installation Repositories", description = "Lists accessible repositories for the installed GitHub App")
    public ResponseEntity<List<GitHubRepositoryDto>> getInstallationRepositories(
            @PathVariable Long id,
            @PathVariable Long installationId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        projectService.getProjectById(id, currentUser);
        List<GitHubRepositoryDto> repos = gitHubIntegrationService.fetchInstallationRepositories(installationId, currentUser.getId());
        return ResponseEntity.ok(repos);
    }

    @PostMapping("/{id}/repositories")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN')")
    @Operation(summary = "Link Repository", description = "Links a GitHub repository and installation to the project")
    public ResponseEntity<RepositoryMappingResponse> linkRepository(
            @PathVariable Long id,
            @Valid @RequestBody LinkRepositoryRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        RepositoryMappingResponse response = projectService.linkRepository(id, request, currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}/repositories")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN')")
    @Operation(summary = "Unlink Repository", description = "Disconnects the repository mapping from the project")
    public ResponseEntity<Map<String, String>> unlinkRepository(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        projectService.unlinkRepository(id, currentUser);
        return ResponseEntity.ok(Map.of("message", "Repository unlinked successfully"));
    }

    @GetMapping("/{id}/dashboard")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'TEAM_LEAD', 'ADMIN')")
    @Operation(summary = "Get Project Dashboard", description = "Returns review metrics, queue counts, and governance stats for the frontend dashboard")
    public ResponseEntity<ProjectDashboardResponse> getProjectDashboard(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ProjectDashboardResponse response = projectService.getProjectDashboard(id, currentUser);
        return ResponseEntity.ok(response);
    }
}
