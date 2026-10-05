package com.sanket.AI.Code.Review.Platform.auth.service.oauth2;

import com.sanket.AI.Code.Review.Platform.auth.dto.response.GitHubUserResponse;
import com.sanket.AI.Code.Review.Platform.user.entity.AuthProvider;

public interface OAuth2ProviderService {

    String getAuthorizationUrl(String state);

    GitHubUserResponse getUserProfile(String code);

    AuthProvider getProvider();
}
