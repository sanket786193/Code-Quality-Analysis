package com.sanket.AI.Code.Review.Platform.notification.repository;

import com.sanket.AI.Code.Review.Platform.notification.entity.Notification;
import com.sanket.AI.Code.Review.Platform.notification.entity.NotificationSeverity;
import com.sanket.AI.Code.Review.Platform.notification.entity.NotificationStatus;
import com.sanket.AI.Code.Review.Platform.notification.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    Page<Notification> findByRecipientIdAndStatusOrderByCreatedAtDesc(
            Long recipientId,
            NotificationStatus status,
            Pageable pageable
    );

    Page<Notification> findByRecipientIdAndTypeOrderByCreatedAtDesc(
            Long recipientId,
            NotificationType type,
            Pageable pageable
    );

    Page<Notification> findByProjectIdOrderByCreatedAtDesc(Long projectId, Pageable pageable);

    long countByRecipientIdAndStatus(Long recipientId, NotificationStatus status);

    long countByRecipientId(Long recipientId);

    long countByRecipientIdAndSeverityAndStatus(Long recipientId, NotificationSeverity severity, NotificationStatus status);

    @Modifying
    @Query("UPDATE Notification n SET n.status = :status, n.readAt = :readAt WHERE n.recipient.id = :recipientId AND n.status = com.sanket.AI.Code.Review.Platform.notification.entity.NotificationStatus.UNREAD")
    int markAllAsReadForUser(
            @Param("recipientId") Long recipientId,
            @Param("status") NotificationStatus status,
            @Param("readAt") LocalDateTime readAt
    );

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.recipient.id = :recipientId")
    void deleteAllByRecipientId(@Param("recipientId") Long recipientId);

    List<Notification> findTop10ByRecipientIdOrderByCreatedAtDesc(Long recipientId);
}
