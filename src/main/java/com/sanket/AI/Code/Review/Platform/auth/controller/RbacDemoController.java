package com.sanket.AI.Code.Review.Platform.auth.controller;

import com.sanket.AI.Code.Review.Platform.common.dto.ApiResponse;
import com.sanket.AI.Code.Review.Platform.exception.ResourceNotFoundException;
import com.sanket.AI.Code.Review.Platform.user.entity.Role;
import com.sanket.AI.Code.Review.Platform.user.entity.User;
import com.sanket.AI.Code.Review.Platform.user.repository.RoleRepository;
import com.sanket.AI.Code.Review.Platform.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/governance")
@RequiredArgsConstructor
@Tag(name = "Role-Based Access Control", description = "Endpoints demonstrating Developer, Team Lead, and Admin role protections")
@SecurityRequirement(name = "BearerAuth")
public class RbacDemoController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @GetMapping("/developer/dashboard")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'TEAM_LEAD', 'ADMIN')")
    @Operation(summary = "Developer basic platform access", description = "Accessible by Developers, Team Leads, and Admins")
    public ResponseEntity<ApiResponse<Map<String, String>>> developerAccess() {
        return ResponseEntity.ok(ApiResponse.success(
                Map.of(
                        "module", "Code Review Workspace",
                        "status", "Authorized",
                        "description", "Welcome Developer! You have basic access to submit code and review assigned pull requests."
                ),
                "Developer access granted"
        ));
    }

    @GetMapping("/team-lead/approvals")
    @PreAuthorize("hasAnyRole('TEAM_LEAD', 'ADMIN')")
    @Operation(summary = "Team Lead review & approve access", description = "Accessible by Team Leads and Admins only")
    public ResponseEntity<ApiResponse<Map<String, String>>> teamLeadAccess() {
        return ResponseEntity.ok(ApiResponse.success(
                Map.of(
                        "module", "Governance & Policy Approval Gate",
                        "status", "Authorized",
                        "description", "Welcome Team Lead! You have permission to override review policies, approve pull requests, and manage code standards."
                ),
                "Team Lead approval access granted"
        ));
    }

    @GetMapping("/admin/system-overview")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin full platform access", description = "Accessible by Admins only")
    public ResponseEntity<ApiResponse<Map<String, Object>>> adminAccess() {
        return ResponseEntity.ok(ApiResponse.success(
                Map.of(
                        "module", "System & Governance Administration",
                        "totalUsers", userRepository.count(),
                        "status", "Authorized",
                        "description", "Welcome Admin! You have full access to platform governance, policies, and role management."
                ),
                "Admin full access granted"
        ));
    }

    @PutMapping("/admin/users/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin user role management", description = "Allows Admins to update user roles")
    public ResponseEntity<ApiResponse<String>> updateUserRoles(
            @PathVariable Long userId,
            @RequestBody Set<String> roleNames) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            String normalizedRole = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
            Role role = roleRepository.findByRoleName(normalizedRole)
                    .orElseGet(() -> roleRepository.save(new Role(normalizedRole, "Custom assigned role")));
            roles.add(role);
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success("Roles updated successfully for user: " + user.getUsername(), "Success"));
    }
}
