package com.sanket.AI.Code.Review.Platform.notification.service;

import com.sanket.AI.Code.Review.Platform.notification.dto.NotificationCountDto;
import com.sanket.AI.Code.Review.Platform.notification.dto.NotificationResponse;
import com.sanket.AI.Code.Review.Platform.notification.dto.SendNotificationRequest;
import com.sanket.AI.Code.Review.Platform.notification.entity.NotificationStatus;
import com.sanket.AI.Code.Review.Platform.notification.entity.NotificationType;
import com.sanket.AI.Code.Review.Platform.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    /**
     * Sends and persists a notification with optional email/queue dispatch.
     */
    NotificationResponse sendNotification(SendNotificationRequest request, UserPrincipal currentUser);

    /**
     * Retrieves paginated notifications filtered by status and type with role-based visibility.
     */
    Page<NotificationResponse> getNotifications(
            NotificationStatus status,
            NotificationType type,
            Pageable pageable,
            UserPrincipal currentUser
    );

    /**
     * Retrieves a single notification by ID.
     */
    NotificationResponse getNotificationById(Long id, UserPrincipal currentUser);

    /**
     * Marks a specific notification as READ.
     */
    NotificationResponse markAsRead(Long id, UserPrincipal currentUser);

    /**
     * Marks all unread notifications as READ for the authenticated user.
     */
    int markAllAsRead(UserPrincipal currentUser);

    /**
     * Deletes a notification by ID.
     */
    void deleteNotification(Long id, UserPrincipal currentUser);

    /**
     * Clears all notifications for the authenticated user.
     */
    void clearAllNotifications(UserPrincipal currentUser);

    /**
     * Retrieves badge counts (unread, total, critical) for notification bell and filter chips.
     */
    NotificationCountDto getNotificationCounts(UserPrincipal currentUser);

    /**
     * Triggers notifications upon completion of an AI code review job.
     */
    void notifyReviewCompleted(String reportReference, Long projectId, String authorUsername, String prTitle, Double score, int criticalCount, int highCount);

    /**
     * Triggers notifications to developer when a Team Lead records an approval or changes request.
     */
    void notifyDecisionMade(String reportReference, Long projectId, String authorUsername, String decision, String comment, String leadUsername);

    /**
     * Triggers notifications to Team Leads when a review is ready for sign-off.
     */
    void notifyDecisionPending(String reportReference, Long projectId, String prTitle, String authorUsername);
}
