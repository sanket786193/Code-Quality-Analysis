package com.sanket.AI.Code.Review.Platform.auth.service.impl;

import com.sanket.AI.Code.Review.Platform.auth.dto.request.ChangePasswordRequest;
import com.sanket.AI.Code.Review.Platform.auth.dto.request.LoginRequest;
import com.sanket.AI.Code.Review.Platform.auth.dto.request.RefreshTokenRequest;
import com.sanket.AI.Code.Review.Platform.auth.dto.request.RegisterRequest;
import com.sanket.AI.Code.Review.Platform.auth.dto.response.AuthResponse;
import com.sanket.AI.Code.Review.Platform.auth.dto.response.GitHubUserResponse;
import com.sanket.AI.Code.Review.Platform.auth.dto.response.RegisterResponse;
import com.sanket.AI.Code.Review.Platform.auth.dto.response.TokenRefreshResponse;
import com.sanket.AI.Code.Review.Platform.auth.entity.RefreshToken;
import com.sanket.AI.Code.Review.Platform.auth.service.AuthService;
import com.sanket.AI.Code.Review.Platform.auth.service.RefreshTokenService;
import com.sanket.AI.Code.Review.Platform.auth.service.oauth2.impl.GitHubOAuth2Service;
import com.sanket.AI.Code.Review.Platform.exception.*;
import com.sanket.AI.Code.Review.Platform.security.JwtTokenProvider;
import com.sanket.AI.Code.Review.Platform.security.UserPrincipal;
import com.sanket.AI.Code.Review.Platform.user.entity.AuthProvider;
import com.sanket.AI.Code.Review.Platform.user.entity.Role;
import com.sanket.AI.Code.Review.Platform.user.entity.RoleType;
import com.sanket.AI.Code.Review.Platform.user.entity.User;
import com.sanket.AI.Code.Review.Platform.user.repository.RoleRepository;
import com.sanket.AI.Code.Review.Platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final GitHubOAuth2Service gitHubOAuth2Service;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Password and confirmation password do not match");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email is already registered: " + request.getEmail());
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUsernameException("Username is already taken: " + request.getUsername());
        }

        Role developerRole = roleRepository.findByRoleName(RoleType.ROLE_DEVELOPER.name())
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .roleName(RoleType.ROLE_DEVELOPER.name())
                        .description("Default developer role with basic platform access")
                        .build()));

        Set<Role> roles = new HashSet<>();
        roles.add(developerRole);

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(request.getUsername().toLowerCase().trim())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .company(request.getCompany())
                .designation(request.getDesignation())
                .githubUsername(request.getGithubUsername())
                .provider(AuthProvider.LOCAL)
                .active(true)
                .accountLocked(false)
                .emailVerified(false)
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Registered new user with id: {} and username: {}", savedUser.getId(), savedUser.getUsername());

        Set<String> roleNames = savedUser.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toSet());

        return RegisterResponse.builder()
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .message("User registered successfully with role " + RoleType.ROLE_DEVELOPER.name())
                .roles(roleNames)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase().trim(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String accessToken = jwtTokenProvider.generateToken(userPrincipal);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userPrincipal.getId());

        Set<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userPrincipal.getId()));

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationMilliseconds() / 1000)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .profileImage(user.getProfileImage())
                .roles(roles)
                .build();
    }

    @Override
    @Transactional
    public TokenRefreshResponse refreshToken(RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    UserPrincipal userPrincipal = UserPrincipal.create(user);
                    String newAccessToken = jwtTokenProvider.generateToken(userPrincipal);
                    // Rotate refresh token
                    RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId());

                    return TokenRefreshResponse.builder()
                            .accessToken(newAccessToken)
                            .refreshToken(newRefreshToken.getToken())
                            .tokenType("Bearer")
                            .expiresIn(jwtTokenProvider.getExpirationMilliseconds() / 1000)
                            .build();
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token is not in database!"));
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        if (StringUtils.hasText(refreshToken)) {
            refreshTokenService.revokeToken(refreshToken);
        }
        SecurityContextHolder.clearContext();
        log.info("User logged out successfully");
    }

    @Override
    public String getGitHubAuthorizationUrl(String state) {
        return gitHubOAuth2Service.getAuthorizationUrl(state);
    }

    @Override
    @Transactional
    public AuthResponse handleGitHubCallback(String code, String state) {
        GitHubUserResponse gitHubUser = gitHubOAuth2Service.getUserProfile(code);
        String githubId = String.valueOf(gitHubUser.getId());
        String githubEmail = gitHubUser.getEmail();

        if (!StringUtils.hasText(githubEmail)) {
            githubEmail = gitHubUser.getLogin() + "@users.noreply.github.com";
        }

        final String finalEmail = githubEmail.toLowerCase().trim();

        // Check if user exists by GitHub ID or by email
        User user = userRepository.findByGithubId(githubId)
                .or(() -> userRepository.findByEmail(finalEmail))
                .orElseGet(() -> createNewGitHubUser(gitHubUser, githubId, finalEmail));

        // If existing local user logging in via GitHub, link GitHub account info
        if (user.getGithubId() == null) {
            user.setGithubId(githubId);
            if (!StringUtils.hasText(user.getGithubUsername())) {
                user.setGithubUsername(gitHubUser.getLogin());
            }
            if (!StringUtils.hasText(user.getProfileImage())) {
                user.setProfileImage(gitHubUser.getAvatarUrl());
            }
            user = userRepository.save(user);
        }

        UserPrincipal userPrincipal = UserPrincipal.create(user);
        String accessToken = jwtTokenProvider.generateToken(userPrincipal);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        Set<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationMilliseconds() / 1000)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .profileImage(user.getProfileImage())
                .roles(roles)
                .build();
    }

    @Override
    @Transactional
    public void changePassword(String currentUsername, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + currentUsername));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new IllegalArgumentException("New password and confirmation password do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed successfully for user: {}", currentUsername);
    }

    private User createNewGitHubUser(GitHubUserResponse gitHubUser, String githubId, String email) {
        Role developerRole = roleRepository.findByRoleName(RoleType.ROLE_DEVELOPER.name())
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .roleName(RoleType.ROLE_DEVELOPER.name())
                        .description("Default developer role with basic platform access")
                        .build()));

        Set<Role> roles = new HashSet<>();
        roles.add(developerRole);

        String username = gitHubUser.getLogin();
        if (userRepository.existsByUsername(username)) {
            username = username + "_" + UUID.randomUUID().toString().substring(0, 4);
        }

        String name = gitHubUser.getName();
        String firstName = username;
        String lastName = "Developer";
        if (StringUtils.hasText(name)) {
            String[] parts = name.trim().split("\\s+", 2);
            firstName = parts[0];
            if (parts.length > 1) {
                lastName = parts[1];
            }
        }

        User newUser = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .username(username.toLowerCase())
                .email(email)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .githubId(githubId)
                .githubUsername(gitHubUser.getLogin())
                .profileImage(gitHubUser.getAvatarUrl())
                .company(gitHubUser.getCompany())
                .provider(AuthProvider.GITHUB)
                .active(true)
                .accountLocked(false)
                .emailVerified(true)
                .roles(roles)
                .build();

        return userRepository.save(newUser);
    }
}
