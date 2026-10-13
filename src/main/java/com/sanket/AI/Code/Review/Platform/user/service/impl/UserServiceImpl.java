package com.sanket.AI.Code.Review.Platform.user.service.impl;

import com.sanket.AI.Code.Review.Platform.exception.DuplicateUsernameException;
import com.sanket.AI.Code.Review.Platform.exception.InvalidRoleException;
import com.sanket.AI.Code.Review.Platform.exception.UserNotFoundException;
import com.sanket.AI.Code.Review.Platform.security.UserPrincipal;
import com.sanket.AI.Code.Review.Platform.user.dto.Request.UpdateProfileRequest;
import com.sanket.AI.Code.Review.Platform.user.dto.Response.RoleResponse;
import com.sanket.AI.Code.Review.Platform.user.dto.Response.UserProfileResponse;
import com.sanket.AI.Code.Review.Platform.user.dto.Response.UserResponse;
import com.sanket.AI.Code.Review.Platform.user.dto.Response.UserSummaryResponse;
import com.sanket.AI.Code.Review.Platform.user.entity.Role;
import com.sanket.AI.Code.Review.Platform.user.entity.User;
import com.sanket.AI.Code.Review.Platform.user.mapper.UserMapper;
import com.sanket.AI.Code.Review.Platform.user.repository.RoleRepository;
import com.sanket.AI.Code.Review.Platform.user.repository.UserRepository;
import com.sanket.AI.Code.Review.Platform.user.service.UserService;
import com.sanket.AI.Code.Review.Platform.user.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile(UserPrincipal currentUser) {
        log.info("Fetching profile for current user ID: {}", currentUser.getId());
        User user = findUserByIdOrThrow(currentUser.getId());
        return userMapper.toUserProfileResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateCurrentUserProfile(UpdateProfileRequest request, UserPrincipal currentUser) {
        log.info("Updating profile for current user ID: {}", currentUser.getId());
        User user = findUserByIdOrThrow(currentUser.getId());

        // Check if username has changed and if the new username already exists
        if (!user.getUsername().equalsIgnoreCase(request.getUsername().trim())) {
            if (userRepository.existsByUsername(request.getUsername().trim())) {
                throw new DuplicateUsernameException("Username '" + request.getUsername() + "' is already in use.");
            }
        }

        userMapper.updateEntityFromProfileRequest(request, user);
        User updatedUser = userRepository.save(user);
        log.info("Successfully updated profile for user: {}", updatedUser.getUsername());
        return userMapper.toUserProfileResponse(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        log.info("Admin fetching all users with pagination");
        return userRepository.findAll(pageable)
                .map(userMapper::toUserResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(String keyword, String role, Boolean active, Pageable pageable) {
        log.info("Searching users with keyword='{}', role='{}', active={}", keyword, role, active);
        Specification<User> spec = Specification.where((Specification<User>) null);

        if (keyword != null && !keyword.isBlank()) {
            spec = spec.and(UserSpecification.searchByKeyword(keyword));
        }

        if (role != null && !role.isBlank()) {
            spec = spec.and(UserSpecification.hasRole(role));
        }

        if (active != null) {
            spec = spec.and(UserSpecification.isActive(active));
        }

        return userRepository.findAll(spec, pageable)
                .map(userMapper::toUserResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        log.info("Fetching user by ID: {}", userId);
        User user = findUserByIdOrThrow(userId);
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUserRoles(Long userId, Set<String> roleNames) {
        log.info("Admin updating roles for user ID: {} with roles: {}", userId, roleNames);
        User user = findUserByIdOrThrow(userId);

        Set<Role> resolvedRoles = new HashSet<>();
        for (String roleName : roleNames) {
            String formattedRole = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
            Role role = roleRepository.findByRoleName(formattedRole)
                    .orElseThrow(() -> new InvalidRoleException("Role not found: " + formattedRole));
            resolvedRoles.add(role);
        }

        user.setRoles(resolvedRoles);
        User savedUser = userRepository.save(user);
        log.info("Updated roles for user: {}", savedUser.getUsername());
        return userMapper.toUserResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(Long userId, boolean active) {
        log.info("Admin updating status for user ID: {} to active={}", userId, active);
        User user = findUserByIdOrThrow(userId);
        user.setActive(active);
        User savedUser = userRepository.save(user);
        return userMapper.toUserResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryResponse> getTeamMembers() {
        log.info("Team lead fetching team members");
        Specification<User> spec = Specification.where(UserSpecification.isActive(true))
                .and(Specification.where(UserSpecification.hasRole("ROLE_DEVELOPER"))
                        .or(UserSpecification.hasRole("ROLE_TEAM_LEAD")));

        return userRepository.findAll(spec).stream()
                .map(userMapper::toUserSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(userMapper::toRoleResponse)
                .collect(Collectors.toList());
    }

    private User findUserByIdOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }
}
