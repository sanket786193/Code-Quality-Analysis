package com.sanket.AI.Code.Review.Platform.notification.dto;

import com.sanket.AI.Code.Review.Platform.notification.entity.NotificationType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationTemplateDto {
    private Long id;
    private String templateCode;
    private String titleTemplate;
    private String bodyTemplate;
    private String emailSubjectTemplate;
    private NotificationType type;
    private Boolean active;
}
