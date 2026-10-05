package com.sanket.AI.Code.Review.Platform.auth.service;

import com.sanket.AI.Code.Review.Platform.auth.dto.request.ChangePasswordRequest;
import com.sanket.AI.Code.Review.Platform.auth.dto.request.LoginRequest;
import com.sanket.AI.Code.Review.Platform.auth.dto.request.RefreshTokenRequest;
import com.sanket.AI.Code.Review.Platform.auth.dto.request.RegisterRequest;
import com.sanket.AI.Code.Review.Platform.auth.dto.response.AuthResponse;
import com.sanket.AI.Code.Review.Platform.auth.dto.response.RegisterResponse;
import com.sanket.AI.Code.Review.Platform.auth.dto.response.TokenRefreshResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    TokenRefreshResponse refreshToken(RefreshTokenRequest request);

    void logout(String refreshToken);

    String getGitHubAuthorizationUrl(String state);

    AuthResponse handleGitHubCallback(String code, String state);

    void changePassword(String currentUsername, ChangePasswordRequest request);
}
