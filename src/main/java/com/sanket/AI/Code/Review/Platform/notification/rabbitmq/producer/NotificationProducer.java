package com.sanket.AI.Code.Review.Platform.notification.rabbitmq.producer;

import com.sanket.AI.Code.Review.Platform.notification.rabbitmq.message.NotificationEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * Publishes notification event asynchronously to decoupled consumers
     * (RabbitMQ queue or Spring event bus).
     */
    public void publishNotificationEvent(NotificationEventMessage message) {
        log.info("Publishing notification event [ID: {}, Type: {}, Recipient: {}]",
                message.getEventId(), message.getType(), message.getRecipientUserId());
        eventPublisher.publishEvent(message);
    }
}
