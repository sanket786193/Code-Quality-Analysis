package com.sanket.AI.Code.Review.Platform.auth.controller;

import com.sanket.AI.Code.Review.Platform.auth.dto.request.ChangePasswordRequest;
import com.sanket.AI.Code.Review.Platform.auth.dto.request.LoginRequest;
import com.sanket.AI.Code.Review.Platform.auth.dto.request.RefreshTokenRequest;
import com.sanket.AI.Code.Review.Platform.auth.dto.request.RegisterRequest;
import com.sanket.AI.Code.Review.Platform.auth.dto.response.AuthResponse;
import com.sanket.AI.Code.Review.Platform.auth.dto.response.GitHubOAuthUrlResponse;
import com.sanket.AI.Code.Review.Platform.auth.dto.response.RegisterResponse;
import com.sanket.AI.Code.Review.Platform.auth.dto.response.TokenRefreshResponse;
import com.sanket.AI.Code.Review.Platform.auth.service.AuthService;
import com.sanket.AI.Code.Review.Platform.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Endpoints for user registration, authentication, token refresh, logout, and GitHub OAuth2")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Registers a new user with default role ROLE_DEVELOPER")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return new ResponseEntity<>(ApiResponse.success(response, "User registered successfully"), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email & password", description = "Authenticates user credentials and returns JWT access token + refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh JWT access token", description = "Generates a new access token and rotated refresh token using an active refresh token")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        TokenRefreshResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Revokes refresh token and invalidates security session")
    public ResponseEntity<ApiResponse<String>> logout(@RequestBody(required = false) RefreshTokenRequest request) {
        String token = request != null ? request.getRefreshToken() : null;
        authService.logout(token);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", "Logout successful"));
    }

    @GetMapping("/oauth2/github/authorize")
    @Operation(summary = "Start GitHub OAuth2 flow", description = "Returns the GitHub OAuth authorization URL or redirects directly to GitHub consent screen")
    public ResponseEntity<ApiResponse<GitHubOAuthUrlResponse>> getGitHubAuthorizationUrl(
            @RequestParam(required = false, defaultValue = "github_auth") String state,
            @RequestParam(required = false, defaultValue = "false") boolean redirect,
            HttpServletResponse httpServletResponse) throws IOException {

        String authUrl = authService.getGitHubAuthorizationUrl(state);
        if (redirect) {
            httpServletResponse.sendRedirect(authUrl);
            return null;
        }

        return ResponseEntity.ok(ApiResponse.success(new GitHubOAuthUrlResponse(authUrl), "GitHub OAuth authorization URL generated"));
    }

    @GetMapping("/oauth2/github/callback")
    @Operation(summary = "GitHub OAuth2 callback", description = "Handles the GitHub OAuth redirect callback, exchanges authorization code for profile, creates/links user, and returns platform JWTs")
    public ResponseEntity<ApiResponse<AuthResponse>> handleGitHubCallback(
            @RequestParam String code,
            @RequestParam(required = false) String state) {
        log.info("Received GitHub OAuth callback with state: {}", state);
        AuthResponse response = authService.handleGitHubCallback(code, state);
        return ResponseEntity.ok(ApiResponse.success(response, "GitHub authentication successful"));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Allows an authenticated user to change their password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", "Success"));
    }
}
