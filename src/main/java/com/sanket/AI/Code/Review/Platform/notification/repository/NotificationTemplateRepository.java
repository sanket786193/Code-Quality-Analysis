package com.sanket.AI.Code.Review.Platform.notification.repository;

import com.sanket.AI.Code.Review.Platform.notification.entity.NotificationTemplate;
import com.sanket.AI.Code.Review.Platform.notification.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    Optional<NotificationTemplate> findByTemplateCode(String templateCode);

    List<NotificationTemplate> findByTypeAndActiveTrue(NotificationType type);
}
