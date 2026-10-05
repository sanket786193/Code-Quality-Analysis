package com.sanket.AI.Code.Review.Platform.project.service;

import com.sanket.AI.Code.Review.Platform.project.dto.request.CreateProjectRequest;
import com.sanket.AI.Code.Review.Platform.project.dto.request.LinkRepositoryRequest;
import com.sanket.AI.Code.Review.Platform.project.dto.request.UpdateProjectRequest;
import com.sanket.AI.Code.Review.Platform.project.dto.response.ProjectDashboardResponse;
import com.sanket.AI.Code.Review.Platform.project.dto.response.ProjectResponse;
import com.sanket.AI.Code.Review.Platform.project.dto.response.RepositoryMappingResponse;
import com.sanket.AI.Code.Review.Platform.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectService {

    ProjectResponse createProject(CreateProjectRequest request, UserPrincipal currentUser);

    ProjectResponse updateProject(Long projectId, UpdateProjectRequest request, UserPrincipal currentUser);

    ProjectResponse getProjectById(Long projectId, UserPrincipal currentUser);

    Page<ProjectResponse> getUserProjects(UserPrincipal currentUser, Pageable pageable);

    void deleteProject(Long projectId, UserPrincipal currentUser);

    RepositoryMappingResponse linkRepository(Long projectId, LinkRepositoryRequest request, UserPrincipal currentUser);

    void unlinkRepository(Long projectId, UserPrincipal currentUser);

    ProjectDashboardResponse getProjectDashboard(Long projectId, UserPrincipal currentUser);
}
