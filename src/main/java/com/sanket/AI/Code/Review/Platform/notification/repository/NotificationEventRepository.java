package com.sanket.AI.Code.Review.Platform.notification.repository;

import com.sanket.AI.Code.Review.Platform.notification.entity.NotificationEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationEventRepository extends JpaRepository<NotificationEvent, Long> {

    Optional<NotificationEvent> findByEventId(String eventId);

    Page<NotificationEvent> findByRecipientUserIdOrderByCreatedAtDesc(Long recipientUserId, Pageable pageable);
}
