package com.sanket.AI.Code.Review.Platform.notification.dto;

import com.sanket.AI.Code.Review.Platform.notification.entity.NotificationSeverity;
import com.sanket.AI.Code.Review.Platform.notification.entity.NotificationStatus;
import com.sanket.AI.Code.Review.Platform.notification.entity.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;
    private Long recipientId;
    private String recipientUsername;
    private String recipientEmail;
    private Long senderId;
    private String senderUsername;
    private Long projectId;
    private String projectName;
    private String reportReference;
    private String title;
    private String message;
    private NotificationType type;
    private NotificationSeverity severity;
    private NotificationStatus status;
    private String actionUrl;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
