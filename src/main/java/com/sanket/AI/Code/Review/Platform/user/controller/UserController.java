package com.sanket.AI.Code.Review.Platform.user.controller;

import com.sanket.AI.Code.Review.Platform.security.UserPrincipal;
import com.sanket.AI.Code.Review.Platform.user.dto.Request.UpdateProfileRequest;
import com.sanket.AI.Code.Review.Platform.user.dto.Request.UpdateUserRolesRequest;
import com.sanket.AI.Code.Review.Platform.user.dto.Request.UpdateUserStatusRequest;
import com.sanket.AI.Code.Review.Platform.user.dto.Response.RoleResponse;
import com.sanket.AI.Code.Review.Platform.user.dto.Response.UserProfileResponse;
import com.sanket.AI.Code.Review.Platform.user.dto.Response.UserResponse;
import com.sanket.AI.Code.Review.Platform.user.dto.Response.UserSummaryResponse;
import com.sanket.AI.Code.Review.Platform.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User & Role Management", description = "Endpoints for user profile, team management, and role-based administration")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get Current User Profile", description = "Fetches the profile details of the authenticated user")
    public ResponseEntity<UserProfileResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal currentUser) {
        UserProfileResponse response = userService.getCurrentUserProfile(currentUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update Current User Profile", description = "Updates profile attributes like name, bio, company, and github username")
    public ResponseEntity<UserProfileResponse> updateCurrentUser(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        UserProfileResponse response = userService.updateCurrentUserProfile(request, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List All Users (Admin)", description = "Admin view of all registered platform users with pagination")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Search Users (Admin)", description = "Admin search and filter by keyword, role, or active status")
    public ResponseEntity<Page<UserResponse>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<UserResponse> users = userService.searchUsers(keyword, role, active, pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEAM_LEAD')")
    @Operation(summary = "Get User by ID", description = "Retrieves user details by ID for Team Leads or Admins")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign/Change User Roles (Admin)", description = "Admin updates the assigned roles for a user")
    public ResponseEntity<UserResponse> updateUserRoles(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRolesRequest request
    ) {
        UserResponse response = userService.updateUserRoles(id, request.getRoles());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate/Deactivate User (Admin)", description = "Admin enables or disables a user account")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        UserResponse response = userService.updateUserStatus(id, request.getActive());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/team-members")
    @PreAuthorize("hasAnyRole('TEAM_LEAD', 'ADMIN')")
    @Operation(summary = "View Team Members (Team Lead/Admin)", description = "Allows Team Leads and Admins to view all developers and leads")
    public ResponseEntity<List<UserSummaryResponse>> getTeamMembers() {
        List<UserSummaryResponse> teamMembers = userService.getTeamMembers();
        return ResponseEntity.ok(teamMembers);
    }

    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List Roles (Admin)", description = "Lists all available roles configured in the system")
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        List<RoleResponse> roles = userService.getAllRoles();
        return ResponseEntity.ok(roles);
    }
}
