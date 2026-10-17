package com.sanket.AI.Code.Review.Platform.notification.controller;

import com.sanket.AI.Code.Review.Platform.common.dto.ApiResponse;
import com.sanket.AI.Code.Review.Platform.notification.dto.NotificationCountDto;
import com.sanket.AI.Code.Review.Platform.notification.dto.NotificationResponse;
import com.sanket.AI.Code.Review.Platform.notification.dto.SendNotificationRequest;
import com.sanket.AI.Code.Review.Platform.notification.entity.NotificationStatus;
import com.sanket.AI.Code.Review.Platform.notification.entity.NotificationType;
import com.sanket.AI.Code.Review.Platform.notification.service.NotificationService;
import com.sanket.AI.Code.Review.Platform.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/notifications", "/api/v1/notifications"})
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notification Module", description = "Endpoints for managing in-app notifications, badge counts, and read states")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('DEVELOPER', 'TEAM_LEAD', 'ADMIN')")
    @Operation(summary = "Get Notifications", description = "Retrieves paginated notifications for current authenticated user with optional status and type filters")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getNotifications(
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(required = false) NotificationType type,
            @PageableDefault(size = 15, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        Page<NotificationResponse> result = notificationService.getNotifications(status, type, pageable, currentUser);
        return ResponseEntity.ok(ApiResponse.success(result, "Notifications retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'TEAM_LEAD', 'ADMIN')")
    @Operation(summary = "Get Notification By ID", description = "Retrieves single notification by ID if user has permission")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotificationById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        NotificationResponse response = notificationService.getNotificationById(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success(response, "Notification retrieved successfully"));
    }

    @PostMapping("/send")
    @PreAuthorize("hasAnyRole('TEAM_LEAD', 'ADMIN')")
    @Operation(summary = "Send Notification", description = "Dispatches a manual or automated notification to a recipient")
    public ResponseEntity<ApiResponse<NotificationResponse>> sendNotification(
            @Valid @RequestBody SendNotificationRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        NotificationResponse response = notificationService.sendNotification(request, currentUser);
        return new ResponseEntity<>(ApiResponse.success(response, "Notification sent successfully"), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'TEAM_LEAD', 'ADMIN')")
    @Operation(summary = "Mark Notification as Read", description = "Marks a single unread notification as read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        NotificationResponse response = notificationService.markAsRead(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success(response, "Notification marked as read"));
    }

    @PatchMapping("/read-all")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'TEAM_LEAD', 'ADMIN')")
    @Operation(summary = "Mark All Notifications as Read", description = "Marks all unread notifications as read for current user")
    public ResponseEntity<ApiResponse<Integer>> markAllAsRead(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        int updatedCount = notificationService.markAllAsRead(currentUser);
        return ResponseEntity.ok(ApiResponse.success(updatedCount, "All notifications marked as read"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'TEAM_LEAD', 'ADMIN')")
    @Operation(summary = "Delete Notification", description = "Deletes a single notification")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        notificationService.deleteNotification(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success(null, "Notification deleted successfully"));
    }

    @DeleteMapping
    @PreAuthorize("hasAnyRole('DEVELOPER', 'TEAM_LEAD', 'ADMIN')")
    @Operation(summary = "Clear All Notifications", description = "Deletes all notifications belonging to current user")
    public ResponseEntity<ApiResponse<Void>> clearAllNotifications(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        notificationService.clearAllNotifications(currentUser);
        return ResponseEntity.ok(ApiResponse.success(null, "All notifications cleared"));
    }

    @GetMapping("/counts")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'TEAM_LEAD', 'ADMIN')")
    @Operation(summary = "Get Notification Badge Counts", description = "Retrieves unread and critical notification counts for top navbar")
    public ResponseEntity<ApiResponse<NotificationCountDto>> getNotificationCounts(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        NotificationCountDto counts = notificationService.getNotificationCounts(currentUser);
        return ResponseEntity.ok(ApiResponse.success(counts, "Notification counts retrieved successfully"));
    }
}
