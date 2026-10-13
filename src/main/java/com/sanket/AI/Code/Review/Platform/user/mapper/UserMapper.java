package com.sanket.AI.Code.Review.Platform.user.mapper;

import com.sanket.AI.Code.Review.Platform.user.dto.Request.UpdateProfileRequest;
import com.sanket.AI.Code.Review.Platform.user.dto.Response.RoleResponse;
import com.sanket.AI.Code.Review.Platform.user.dto.Response.UserProfileResponse;
import com.sanket.AI.Code.Review.Platform.user.dto.Response.UserResponse;
import com.sanket.AI.Code.Review.Platform.user.dto.Response.UserSummaryResponse;
import com.sanket.AI.Code.Review.Platform.user.entity.Role;
import com.sanket.AI.Code.Review.Platform.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserProfileResponse toUserProfileResponse(User user) {
        if (user == null) {
            return null;
        }

        return UserProfileResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .email(user.getEmail())
                .profileImage(user.getProfileImage())
                .githubUsername(user.getGithubUsername())
                .githubId(user.getGithubId())
                .provider(user.getProvider())
                .company(user.getCompany())
                .designation(user.getDesignation())
                .active(user.getActive())
                .emailVerified(user.getEmailVerified())
                .roles(extractRoleNames(user.getRoles()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public UserResponse toUserResponse(User user) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .email(user.getEmail())
                .profileImage(user.getProfileImage())
                .githubUsername(user.getGithubUsername())
                .githubId(user.getGithubId())
                .provider(user.getProvider())
                .company(user.getCompany())
                .designation(user.getDesignation())
                .active(user.getActive())
                .accountLocked(user.getAccountLocked())
                .emailVerified(user.getEmailVerified())
                .roles(extractRoleNames(user.getRoles()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public UserSummaryResponse toUserSummaryResponse(User user) {
        if (user == null) {
            return null;
        }

        return UserSummaryResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .email(user.getEmail())
                .designation(user.getDesignation())
                .company(user.getCompany())
                .profileImage(user.getProfileImage())
                .active(user.getActive())
                .roles(extractRoleNames(user.getRoles()))
                .build();
    }

    public RoleResponse toRoleResponse(Role role) {
        if (role == null) {
            return null;
        }

        return RoleResponse.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .description(role.getDescription())
                .build();
    }

    public void updateEntityFromProfileRequest(UpdateProfileRequest request, User user) {
        if (request == null || user == null) {
            return;
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUsername(request.getUsername());
        user.setGithubUsername(request.getGithubUsername());
        user.setCompany(request.getCompany());
        user.setDesignation(request.getDesignation());
        if (request.getProfileImage() != null) {
            user.setProfileImage(request.getProfileImage());
        }
    }

    private Set<String> extractRoleNames(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return Collections.emptySet();
        }
        return roles.stream()
                .map(Role::getRoleName)
                .collect(Collectors.toSet());
    }
}
