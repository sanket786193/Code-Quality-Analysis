package com.sanket.AI.Code.Review.Platform.notification.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification_events", indexes = {
        @Index(name = "idx_notif_event_id", columnList = "eventId"),
        @Index(name = "idx_notif_event_recipient", columnList = "recipientUserId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private NotificationType type;

    @Column(nullable = false)
    private Long recipientUserId;

    private Long senderUserId;

    private Long projectId;

    private String reportReference;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 16)
    private NotificationSeverity severity = NotificationSeverity.INFO;

    @Builder.Default
    private Boolean sentViaEmail = false;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.eventId == null || this.eventId.isBlank()) {
            this.eventId = "EVT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
