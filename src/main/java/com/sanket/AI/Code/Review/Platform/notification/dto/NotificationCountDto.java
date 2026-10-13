package com.sanket.AI.Code.Review.Platform.notification.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationCountDto {
    private long unreadCount;
    private long totalCount;
    private long criticalCount;
}
