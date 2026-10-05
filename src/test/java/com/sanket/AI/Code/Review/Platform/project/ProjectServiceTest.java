package com.sanket.AI.Code.Review.Platform.project;

import com.sanket.AI.Code.Review.Platform.exception.DuplicateProjectNameException;
import com.sanket.AI.Code.Review.Platform.github.model.GitHubInstallation;
import com.sanket.AI.Code.Review.Platform.github.repository.GitHubInstallationRepository;
import com.sanket.AI.Code.Review.Platform.project.dto.request.CreateProjectRequest;
import com.sanket.AI.Code.Review.Platform.project.dto.request.LinkRepositoryRequest;
import com.sanket.AI.Code.Review.Platform.project.dto.response.ProjectResponse;
import com.sanket.AI.Code.Review.Platform.project.dto.response.RepositoryMappingResponse;
import com.sanket.AI.Code.Review.Platform.project.entity.Project;
import com.sanket.AI.Code.Review.Platform.project.entity.ProjectStatus;
import com.sanket.AI.Code.Review.Platform.project.entity.RepositoryMapping;
import com.sanket.AI.Code.Review.Platform.project.mapper.ProjectMapper;
import com.sanket.AI.Code.Review.Platform.project.repository.ProjectRepository;
import com.sanket.AI.Code.Review.Platform.project.repository.RepositoryMappingRepository;
import com.sanket.AI.Code.Review.Platform.project.service.ProjectServiceImpl;
import com.sanket.AI.Code.Review.Platform.review.mapper.ReviewMapper;
import com.sanket.AI.Code.Review.Platform.review.repository.ReviewFindingRepository;
import com.sanket.AI.Code.Review.Platform.review.repository.ReviewJobRepository;
import com.sanket.AI.Code.Review.Platform.security.UserPrincipal;
import com.sanket.AI.Code.Review.Platform.user.entity.User;
import com.sanket.AI.Code.Review.Platform.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private RepositoryMappingRepository repositoryMappingRepository;

    @Mock
    private GitHubInstallationRepository installationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewJobRepository reviewJobRepository;

    @Mock
    private ReviewFindingRepository reviewFindingRepository;

    @Spy
    private ProjectMapper projectMapper = new ProjectMapper();

    @Spy
    private ReviewMapper reviewMapper = new ReviewMapper();

    @InjectMocks
    private ProjectServiceImpl projectService;

    private UserPrincipal developerUser;
    private User testUser;

    @BeforeEach
    void setUp() {
        developerUser = UserPrincipal.builder()
                .id(1L)
                .username("dev_user")
                .email("dev@example.com")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_DEVELOPER")))
                .build();

        testUser = User.builder()
                .id(1L)
                .username("dev_user")
                .email("dev@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();
    }

    @Test
    void createProject_Success() {
        CreateProjectRequest request = CreateProjectRequest.builder()
                .name("AI Governance Engine")
                .description("Test Description")
                .build();

        when(projectRepository.existsByNameAndOwnerId("AI Governance Engine", 1L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project p = invocation.getArgument(0);
            p.setId(10L);
            return p;
        });

        ProjectResponse response = projectService.createProject(request, developerUser);

        assertNotNull(response);
        assertEquals("AI Governance Engine", response.getName());
        assertEquals(ProjectStatus.ACTIVE, response.getStatus());
        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    void createProject_DuplicateName_ThrowsException() {
        CreateProjectRequest request = CreateProjectRequest.builder()
                .name("Duplicate Project")
                .build();

        when(projectRepository.existsByNameAndOwnerId("Duplicate Project", 1L)).thenReturn(true);

        assertThrows(DuplicateProjectNameException.class, () -> projectService.createProject(request, developerUser));
        verify(projectRepository, never()).save(any());
    }

    @Test
    void linkRepository_Success() {
        Project project = Project.builder()
                .id(10L)
                .name("AI Governance Engine")
                .owner(testUser)
                .build();

        GitHubInstallation installation = GitHubInstallation.builder()
                .id(5L)
                .installationId(998877L)
                .user(testUser)
                .build();

        LinkRepositoryRequest linkRequest = LinkRepositoryRequest.builder()
                .installationId(998877L)
                .githubRepoId(123456L)
                .repoName("ai-code-review")
                .repoFullName("dev_user/ai-code-review")
                .repoUrl("https://github.com/dev_user/ai-code-review")
                .defaultBranch("main")
                .isPrivate(true)
                .build();

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(installationRepository.findByInstallationId(998877L)).thenReturn(Optional.of(installation));
        when(repositoryMappingRepository.findByGithubRepoId(123456L)).thenReturn(Optional.empty());
        when(repositoryMappingRepository.save(any(RepositoryMapping.class))).thenAnswer(invocation -> {
            RepositoryMapping rm = invocation.getArgument(0);
            rm.setId(100L);
            return rm;
        });

        RepositoryMappingResponse response = projectService.linkRepository(10L, linkRequest, developerUser);

        assertNotNull(response);
        assertEquals("dev_user/ai-code-review", response.getRepoFullName());
        assertTrue(response.getWebhookActive());
        verify(repositoryMappingRepository, times(1)).save(any(RepositoryMapping.class));
    }
}
