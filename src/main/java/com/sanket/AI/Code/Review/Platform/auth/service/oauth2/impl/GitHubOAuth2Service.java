package com.sanket.AI.Code.Review.Platform.auth.service.oauth2.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sanket.AI.Code.Review.Platform.auth.dto.response.GitHubUserResponse;
import com.sanket.AI.Code.Review.Platform.auth.service.oauth2.OAuth2ProviderService;
import com.sanket.AI.Code.Review.Platform.exception.OAuth2AuthenticationException;
import com.sanket.AI.Code.Review.Platform.user.entity.AuthProvider;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GitHubOAuth2Service implements OAuth2ProviderService {

    private static final String GITHUB_AUTH_URL = "https://github.com/login/oauth/authorize";
    private static final String GITHUB_TOKEN_URL = "https://github.com/login/oauth/access_token";
    private static final String GITHUB_USER_URL = "https://api.github.com/user";
    private static final String GITHUB_USER_EMAILS_URL = "https://api.github.com/user/emails";

    @Value("${app.oauth2.github.client-id:dummy_client_id}")
    private String clientId;

    @Value("${app.oauth2.github.client-secret:dummy_client_secret}")
    private String clientSecret;

    @Value("${app.oauth2.github.redirect-uri:http://localhost:8080/api/auth/oauth2/github/callback}")
    private String redirectUri;

    private final RestClient restClient = RestClient.create();

    @Override
    public AuthProvider getProvider() {
        return AuthProvider.GITHUB;
    }

    @Override
    public String getAuthorizationUrl(String state) {
        return UriComponentsBuilder.fromUriString(GITHUB_AUTH_URL)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", "read:user,user:email")
                .queryParam("state", StringUtils.hasText(state) ? state : "github_auth")
                .build()
                .toUriString();
    }

    @Override
    public GitHubUserResponse getUserProfile(String code) {
        String accessToken = exchangeCodeForAccessToken(code);
        GitHubUserResponse profile = fetchGitHubUserProfile(accessToken);

        if (!StringUtils.hasText(profile.getEmail())) {
            String primaryEmail = fetchPrimaryEmail(accessToken);
            profile.setEmail(primaryEmail);
        }

        return profile;
    }

    private String exchangeCodeForAccessToken(String code) {
        try {
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("code", code);
            body.add("redirect_uri", redirectUri);

            Map<String, Object> response = restClient.post()
                    .uri(GITHUB_TOKEN_URL)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            if (response == null || !response.containsKey("access_token")) {
                String error = response != null && response.containsKey("error_description")
                        ? (String) response.get("error_description")
                        : "Failed to exchange GitHub authorization code for access token";
                throw new OAuth2AuthenticationException(error);
            }

            return (String) response.get("access_token");
        } catch (Exception ex) {
            log.error("Error exchanging GitHub OAuth code: {}", ex.getMessage());
            throw new OAuth2AuthenticationException("Failed to exchange authorization code with GitHub: " + ex.getMessage(), ex);
        }
    }

    private GitHubUserResponse fetchGitHubUserProfile(String accessToken) {
        try {
            return restClient.get()
                    .uri(GITHUB_USER_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .header(HttpHeaders.ACCEPT, "application/vnd.github+json")
                    .retrieve()
                    .body(GitHubUserResponse.class);
        } catch (Exception ex) {
            log.error("Error fetching GitHub profile: {}", ex.getMessage());
            throw new OAuth2AuthenticationException("Failed to retrieve profile from GitHub: " + ex.getMessage(), ex);
        }
    }

    private String fetchPrimaryEmail(String accessToken) {
        try {
            List<GitHubEmail> emails = restClient.get()
                    .uri(GITHUB_USER_EMAILS_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .header(HttpHeaders.ACCEPT, "application/vnd.github+json")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<GitHubEmail>>() {});

            if (emails != null) {
                return emails.stream()
                        .filter(GitHubEmail::isPrimary)
                        .map(GitHubEmail::getEmail)
                        .findFirst()
                        .orElse(emails.isEmpty() ? null : emails.get(0).getEmail());
            }
        } catch (Exception ex) {
            log.warn("Could not fetch user emails from GitHub: {}", ex.getMessage());
        }
        return null;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GitHubEmail {
        private String email;
        private boolean primary;
        private boolean verified;
    }
}
