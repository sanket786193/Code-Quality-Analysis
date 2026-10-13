package com.sanket.AI.Code.Review.Platform.user.service;

import com.sanket.AI.Code.Review.Platform.security.UserPrincipal;
import com.sanket.AI.Code.Review.Platform.user.dto.Request.UpdateProfileRequest;
import com.sanket.AI.Code.Review.Platform.user.dto.Response.RoleResponse;
import com.sanket.AI.Code.Review.Platform.user.dto.Response.UserProfileResponse;
import com.sanket.AI.Code.Review.Platform.user.dto.Response.UserResponse;
import com.sanket.AI.Code.Review.Platform.user.dto.Response.UserSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface UserService {

    UserProfileResponse getCurrentUserProfile(UserPrincipal currentUser);

    UserProfileResponse updateCurrentUserProfile(UpdateProfileRequest request, UserPrincipal currentUser);

    Page<UserResponse> getAllUsers(Pageable pageable);

    Page<UserResponse> searchUsers(String keyword, String role, Boolean active, Pageable pageable);

    UserResponse getUserById(Long userId);

    UserResponse updateUserRoles(Long userId, Set<String> roleNames);

    UserResponse updateUserStatus(Long userId, boolean active);

    List<UserSummaryResponse> getTeamMembers();

    List<RoleResponse> getAllRoles();
}
