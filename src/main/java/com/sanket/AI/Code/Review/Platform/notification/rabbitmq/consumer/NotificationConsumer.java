package com.sanket.AI.Code.Review.Platform.notification.rabbitmq.consumer;

import com.sanket.AI.Code.Review.Platform.notification.rabbitmq.message.NotificationEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    /**
     * Handles async event messages dispatched from notification producer.
     * Can be annotated with @RabbitListener for external broker deployment.
     */
    @Async
    @EventListener
    public void handleNotificationMessage(NotificationEventMessage message) {
        log.info("Processing asynchronous notification event [EventId: {}, Type: {}, Recipient: {}]",
                message.getEventId(), message.getType(), message.getRecipientUserId());

        // Process asynchronous notification integrations (e.g. WebSocket push, external audit logger)
    }
}
