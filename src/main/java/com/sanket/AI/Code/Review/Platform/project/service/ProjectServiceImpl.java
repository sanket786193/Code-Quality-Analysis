package com.sanket.AI.Code.Review.Platform.project.service;

import com.sanket.AI.Code.Review.Platform.exception.*;
import com.sanket.AI.Code.Review.Platform.github.model.GitHubInstallation;
import com.sanket.AI.Code.Review.Platform.github.repository.GitHubInstallationRepository;
import com.sanket.AI.Code.Review.Platform.project.dto.request.CreateProjectRequest;
import com.sanket.AI.Code.Review.Platform.project.dto.request.LinkRepositoryRequest;
import com.sanket.AI.Code.Review.Platform.project.dto.request.UpdateProjectRequest;
import com.sanket.AI.Code.Review.Platform.project.dto.response.ProjectDashboardResponse;
import com.sanket.AI.Code.Review.Platform.project.dto.response.ProjectResponse;
import com.sanket.AI.Code.Review.Platform.project.dto.response.RepositoryMappingResponse;
import com.sanket.AI.Code.Review.Platform.project.entity.Project;
import com.sanket.AI.Code.Review.Platform.project.entity.ProjectStatus;
import com.sanket.AI.Code.Review.Platform.project.entity.RepositoryMapping;
import com.sanket.AI.Code.Review.Platform.project.mapper.ProjectMapper;
import com.sanket.AI.Code.Review.Platform.project.repository.ProjectRepository;
import com.sanket.AI.Code.Review.Platform.project.repository.RepositoryMappingRepository;
import com.sanket.AI.Code.Review.Platform.review.dto.response.ReviewJobResponse;
import com.sanket.AI.Code.Review.Platform.review.entity.FindingSeverity;
import com.sanket.AI.Code.Review.Platform.review.entity.ReviewJobStatus;
import com.sanket.AI.Code.Review.Platform.review.mapper.ReviewMapper;
import com.sanket.AI.Code.Review.Platform.review.repository.ReviewFindingRepository;
import com.sanket.AI.Code.Review.Platform.review.repository.ReviewJobRepository;
import com.sanket.AI.Code.Review.Platform.security.UserPrincipal;
import com.sanket.AI.Code.Review.Platform.user.entity.User;
import com.sanket.AI.Code.Review.Platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final RepositoryMappingRepository repositoryMappingRepository;
    private final GitHubInstallationRepository installationRepository;
    private final UserRepository userRepository;
    private final ReviewJobRepository reviewJobRepository;
    private final ReviewFindingRepository reviewFindingRepository;
    private final ProjectMapper projectMapper;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, UserPrincipal currentUser) {
        log.info("Creating project '{}' for user id {}", request.getName(), currentUser.getId());

        if (projectRepository.existsByNameAndOwnerId(request.getName(), currentUser.getId())) {
            throw new DuplicateProjectNameException("A project with name '" + request.getName() + "' already exists for your account.");
        }

        User owner = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUser.getId()));

        User teamLead = null;
        if (request.getTeamLeadId() != null) {
            teamLead = userRepository.findById(request.getTeamLeadId())
                    .orElseThrow(() -> new ResourceNotFoundException("Team lead user not found with id: " + request.getTeamLeadId()));
        }

        Project project = Project.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .owner(owner)
                .teamLead(teamLead)
                .status(ProjectStatus.ACTIVE)
                .build();

        Project savedProject = projectRepository.save(project);
        log.info("Successfully created project with ID {}", savedProject.getId());

        return projectMapper.toProjectResponse(savedProject);
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(Long projectId, UpdateProjectRequest request, UserPrincipal currentUser) {
        Project project = findProjectOrThrow(projectId);
        validateProjectManagementAccess(project, currentUser);

        if (!project.getName().equalsIgnoreCase(request.getName().trim())) {
            if (projectRepository.existsByNameAndOwnerId(request.getName().trim(), project.getOwner().getId())) {
                throw new DuplicateProjectNameException("Project name '" + request.getName() + "' is already in use.");
            }
            project.setName(request.getName().trim());
        }

        project.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }

        if (request.getTeamLeadId() != null) {
            User teamLead = userRepository.findById(request.getTeamLeadId())
                    .orElseThrow(() -> new ResourceNotFoundException("Team lead not found with id: " + request.getTeamLeadId()));
            project.setTeamLead(teamLead);
        }

        Project updatedProject = projectRepository.save(project);
        log.info("Updated project ID {}", projectId);
        return projectMapper.toProjectResponse(updatedProject);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long projectId, UserPrincipal currentUser) {
        Project project = findProjectOrThrow(projectId);
        validateProjectViewAccess(project, currentUser);
        return projectMapper.toProjectResponse(project);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponse> getUserProjects(UserPrincipal currentUser, Pageable pageable) {
        boolean isAdmin = hasRole(currentUser, "ROLE_ADMIN");
        boolean isTeamLead = hasRole(currentUser, "ROLE_TEAM_LEAD");

        Page<Project> page;
        if (isAdmin) {
            page = projectRepository.findAll(pageable);
        } else if (isTeamLead) {
            page = projectRepository.findByOwnerIdOrTeamLeadId(currentUser.getId(), currentUser.getId(), pageable);
        } else {
            page = projectRepository.findByOwnerId(currentUser.getId(), pageable);
        }

        return page.map(projectMapper::toProjectResponse);
    }

    @Override
    @Transactional
    public void deleteProject(Long projectId, UserPrincipal currentUser) {
        Project project = findProjectOrThrow(projectId);
        validateProjectManagementAccess(project, currentUser);

        projectRepository.delete(project);
        log.info("Deleted project with ID {}", projectId);
    }

    @Override
    @Transactional
    public RepositoryMappingResponse linkRepository(Long projectId, LinkRepositoryRequest request, UserPrincipal currentUser) {
        Project project = findProjectOrThrow(projectId);
        validateProjectManagementAccess(project, currentUser);

        GitHubInstallation installation = installationRepository.findByInstallationId(request.getInstallationId())
                .orElseThrow(() -> new GitHubInstallationException("GitHub installation not found for ID: " + request.getInstallationId()));

        // Check if repository is already bound to another project
        repositoryMappingRepository.findByGithubRepoId(request.getGithubRepoId())
                .ifPresent(existing -> {
                    if (!existing.getProject().getId().equals(projectId)) {
                        throw new InvalidProjectException("Repository " + request.getRepoFullName() +
                                " is already linked to project: " + existing.getProject().getName());
                    }
                });

        RepositoryMapping mapping = project.getRepositoryMapping();
        if (mapping == null) {
            mapping = RepositoryMapping.builder()
                    .project(project)
                    .installation(installation)
                    .githubRepoId(request.getGithubRepoId())
                    .repoName(request.getRepoName())
                    .repoFullName(request.getRepoFullName())
                    .repoUrl(request.getRepoUrl())
                    .defaultBranch(request.getDefaultBranch() != null ? request.getDefaultBranch() : "main")
                    .isPrivate(request.getIsPrivate() != null ? request.getIsPrivate() : false)
                    .webhookActive(true)
                    .build();
            project.setRepositoryMapping(mapping);
        } else {
            mapping.setInstallation(installation);
            mapping.setGithubRepoId(request.getGithubRepoId());
            mapping.setRepoName(request.getRepoName());
            mapping.setRepoFullName(request.getRepoFullName());
            mapping.setRepoUrl(request.getRepoUrl());
            mapping.setDefaultBranch(request.getDefaultBranch() != null ? request.getDefaultBranch() : "main");
            mapping.setIsPrivate(request.getIsPrivate() != null ? request.getIsPrivate() : false);
            mapping.setWebhookActive(true);
        }

        RepositoryMapping savedMapping = repositoryMappingRepository.save(mapping);
        projectRepository.save(project);

        log.info("Linked GitHub repository '{}' to project ID {}", request.getRepoFullName(), projectId);
        return projectMapper.toRepositoryMappingResponse(savedMapping);
    }

    @Override
    @Transactional
    public void unlinkRepository(Long projectId, UserPrincipal currentUser) {
        Project project = findProjectOrThrow(projectId);
        validateProjectManagementAccess(project, currentUser);

        RepositoryMapping mapping = project.getRepositoryMapping();
        if (mapping == null) {
            throw new RepositoryNotLinkedException("Project '" + project.getName() + "' does not have a linked repository.");
        }

        project.setRepositoryMapping(null);
        repositoryMappingRepository.delete(mapping);
        projectRepository.save(project);

        log.info("Unlinked repository from project ID {}", projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDashboardResponse getProjectDashboard(Long projectId, UserPrincipal currentUser) {
        Project project = findProjectOrThrow(projectId);
        validateProjectViewAccess(project, currentUser);

        long totalReviews = reviewJobRepository.countByProjectId(projectId);
        long queuedReviews = reviewJobRepository.countByProjectIdAndStatus(projectId, ReviewJobStatus.QUEUED);
        long processingReviews = reviewJobRepository.countByProjectIdAndStatus(projectId, ReviewJobStatus.PROCESSING);
        long completedReviews = reviewJobRepository.countByProjectIdAndStatus(projectId, ReviewJobStatus.COMPLETED);
        long failedReviews = reviewJobRepository.countByProjectIdAndStatus(projectId, ReviewJobStatus.FAILED);
        long criticalFindings = reviewFindingRepository.countByReviewJobProjectIdAndSeverity(projectId, FindingSeverity.CRITICAL);

        List<ReviewJobResponse> recentReviews = reviewJobRepository.findTop5ByProjectIdOrderByQueuedAtDesc(projectId)
                .stream()
                .map(reviewMapper::toReviewJobResponse)
                .collect(Collectors.toList());

        return ProjectDashboardResponse.builder()
                .project(projectMapper.toProjectResponse(project))
                .totalReviews(totalReviews)
                .queuedReviews(queuedReviews)
                .processingReviews(processingReviews)
                .completedReviews(completedReviews)
                .failedReviews(failedReviews)
                .criticalFindingsCount(criticalFindings)
                .recentReviewJobs(recentReviews)
                .build();
    }

    private Project findProjectOrThrow(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new InvalidProjectException("Project not found with id: " + projectId));
    }

    private void validateProjectManagementAccess(Project project, UserPrincipal currentUser) {
        if (hasRole(currentUser, "ROLE_ADMIN")) {
            return;
        }
        if (!project.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedProjectAccessException("You do not have permission to manage this project.");
        }
    }

    private void validateProjectViewAccess(Project project, UserPrincipal currentUser) {
        if (hasRole(currentUser, "ROLE_ADMIN")) {
            return;
        }
        if (project.getOwner().getId().equals(currentUser.getId())) {
            return;
        }
        if (project.getTeamLead() != null && project.getTeamLead().getId().equals(currentUser.getId())) {
            return;
        }
        throw new UnauthorizedProjectAccessException("You do not have permission to view this project.");
    }

    private boolean hasRole(UserPrincipal user, String roleName) {
        if (user == null || user.getAuthorities() == null) return false;
        return user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals(roleName));
    }
}
