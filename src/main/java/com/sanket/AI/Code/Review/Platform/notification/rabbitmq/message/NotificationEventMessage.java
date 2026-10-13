package com.sanket.AI.Code.Review.Platform.notification.rabbitmq.message;

import com.sanket.AI.Code.Review.Platform.notification.entity.NotificationSeverity;
import com.sanket.AI.Code.Review.Platform.notification.entity.NotificationType;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEventMessage implements Serializable {

    private String eventId;
    private Long recipientUserId;
    private String recipientEmail;
    private Long senderUserId;
    private Long projectId;
    private String reportReference;
    private NotificationType type;
    private NotificationSeverity severity;
    private String title;
    private String message;
    private String actionUrl;
    private boolean sendEmail;
    private LocalDateTime timestamp;
}
