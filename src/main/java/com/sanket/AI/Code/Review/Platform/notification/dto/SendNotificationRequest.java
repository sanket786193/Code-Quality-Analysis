package com.sanket.AI.Code.Review.Platform.notification.dto;

import com.sanket.AI.Code.Review.Platform.notification.entity.NotificationSeverity;
import com.sanket.AI.Code.Review.Platform.notification.entity.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendNotificationRequest {

    @NotNull(message = "Recipient user ID is required")
    private Long recipientId;

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    @NotBlank(message = "Notification title cannot be blank")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;

    @NotBlank(message = "Notification message cannot be blank")
    private String message;

    @Builder.Default
    private NotificationSeverity severity = NotificationSeverity.INFO;

    private String actionUrl;

    private Long projectId;

    private String reportReference;

    @Builder.Default
    private Boolean sendEmail = false;
}
