package com.sanket.AI.Code.Review.Platform.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanket.AI.Code.Review.Platform.auth.dto.request.LoginRequest;
import com.sanket.AI.Code.Review.Platform.auth.dto.request.RefreshTokenRequest;
import com.sanket.AI.Code.Review.Platform.auth.dto.request.RegisterRequest;
import com.sanket.AI.Code.Review.Platform.auth.dto.response.AuthResponse;
import com.sanket.AI.Code.Review.Platform.common.dto.ApiResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthenticationIntegrationTests {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private String savedAccessToken;
    private String savedRefreshToken;

    @BeforeEach
    void setUp() {
        if (mockMvc == null) {
            mockMvc = MockMvcBuilders
                    .webAppContextSetup(context)
                    .apply(springSecurity())
                    .build();
        }
    }

    @Test
    @Order(1)
    void shouldRegisterNewUserWithDefaultDeveloperRole() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .firstName("Alex")
                .lastName("Rivera")
                .username("alex_dev_" + System.currentTimeMillis())
                .email("alex." + System.currentTimeMillis() + "@platform.dev")
                .password("Password123!")
                .confirmPassword("Password123!")
                .company("Acme Corp")
                .designation("Senior Backend Engineer")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.roles[0]").value("ROLE_DEVELOPER"));
    }

    @Test
    @Order(2)
    void shouldLoginSuccessfullyAndReturnJwtAndRefreshToken() throws Exception {
        String email = "login.test." + System.currentTimeMillis() + "@platform.dev";
        String username = "login_user_" + System.currentTimeMillis();

        RegisterRequest registerRequest = RegisterRequest.builder()
                .firstName("Sam")
                .lastName("Smith")
                .username(username)
                .email(email)
                .password("Secr3tP@ss")
                .confirmPassword("Secr3tP@ss")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = LoginRequest.builder()
                .email(email)
                .password("Secr3tP@ss")
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        ApiResponse<?> apiResponse = objectMapper.readValue(responseJson, ApiResponse.class);
        AuthResponse authResponse = objectMapper.convertValue(apiResponse.getData(), AuthResponse.class);

        savedAccessToken = authResponse.getAccessToken();
        savedRefreshToken = authResponse.getRefreshToken();

        assertThat(savedAccessToken).isNotBlank();
        assertThat(savedRefreshToken).isNotBlank();
    }

    @Test
    @Order(3)
    void shouldRejectLoginWithInvalidPassword() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("alex.dev@platform.dev")
                .password("WrongPassword")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @Order(4)
    void shouldRefreshTokenSuccessfully() throws Exception {
        assertThat(savedRefreshToken).isNotNull();

        RefreshTokenRequest refreshRequest = RefreshTokenRequest.builder()
                .refreshToken(savedRefreshToken)
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        assertThat(responseJson).contains("accessToken");
    }

    @Test
    @Order(5)
    void shouldAllowDeveloperAccessWithToken() throws Exception {
        assertThat(savedAccessToken).isNotNull();

        mockMvc.perform(get("/api/governance/developer/dashboard")
                        .header("Authorization", "Bearer " + savedAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.module").value("Code Review Workspace"));
    }

    @Test
    @Order(6)
    void shouldForbidDeveloperFromAdminEndpoint() throws Exception {
        assertThat(savedAccessToken).isNotNull();

        mockMvc.perform(get("/api/governance/admin/system-overview")
                        .header("Authorization", "Bearer " + savedAccessToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @Order(7)
    void shouldGenerateGitHubOAuthStartUrl() throws Exception {
        mockMvc.perform(get("/api/auth/oauth2/github/authorize"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.authorizationUrl").value(org.hamcrest.Matchers.containsString("github.com/login/oauth/authorize")));
    }

    @Test
    @Order(8)
    void shouldLogoutAndRevokeToken() throws Exception {
        RefreshTokenRequest logoutRequest = RefreshTokenRequest.builder()
                .refreshToken(savedRefreshToken)
                .build();

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
