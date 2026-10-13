package com.sanket.AI.Code.Review.Platform.notification.mapper;

import com.sanket.AI.Code.Review.Platform.notification.dto.NotificationResponse;
import com.sanket.AI.Code.Review.Platform.notification.dto.NotificationTemplateDto;
import com.sanket.AI.Code.Review.Platform.notification.dto.SendNotificationRequest;
import com.sanket.AI.Code.Review.Platform.notification.entity.Notification;
import com.sanket.AI.Code.Review.Platform.notification.entity.NotificationStatus;
import com.sanket.AI.Code.Review.Platform.notification.entity.NotificationTemplate;
import com.sanket.AI.Code.Review.Platform.project.entity.Project;
import com.sanket.AI.Code.Review.Platform.user.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        if (notification == null) return null;

        return NotificationResponse.builder()
                .id(notification.getId())
                .recipientId(notification.getRecipient() != null ? notification.getRecipient().getId() : null)
                .recipientUsername(notification.getRecipient() != null ? notification.getRecipient().getUsername() : null)
                .recipientEmail(notification.getRecipient() != null ? notification.getRecipient().getEmail() : null)
                .senderId(notification.getSender() != null ? notification.getSender().getId() : null)
                .senderUsername(notification.getSender() != null ? notification.getSender().getUsername() : "System")
                .projectId(notification.getProject() != null ? notification.getProject().getId() : null)
                .projectName(notification.getProject() != null ? notification.getProject().getName() : null)
                .reportReference(notification.getReportReference())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .severity(notification.getSeverity())
                .status(notification.getStatus())
                .actionUrl(notification.getActionUrl())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }

    public Notification toEntity(SendNotificationRequest request, User recipient, User sender, Project project) {
        if (request == null) return null;

        return Notification.builder()
                .recipient(recipient)
                .sender(sender)
                .project(project)
                .reportReference(request.getReportReference())
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType())
                .severity(request.getSeverity())
                .status(NotificationStatus.UNREAD)
                .actionUrl(request.getActionUrl())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public NotificationTemplateDto toTemplateDto(NotificationTemplate template) {
        if (template == null) return null;

        return NotificationTemplateDto.builder()
                .id(template.getId())
                .templateCode(template.getTemplateCode())
                .titleTemplate(template.getTitleTemplate())
                .bodyTemplate(template.getBodyTemplate())
                .emailSubjectTemplate(template.getEmailSubjectTemplate())
                .type(template.getType())
                .active(template.getActive())
                .build();
    }
}
