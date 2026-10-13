package com.sanket.AI.Code.Review.Platform.review.rabbitmq.producer;

import com.sanket.AI.Code.Review.Platform.review.rabbitmq.message.ReviewJobMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewJobProducer {

    private final ApplicationEventPublisher eventPublisher;

    public void publishReviewJob(ReviewJobMessage message) {
        log.info("Enqueuing review job message for reference: {}", message.getJobReference());
        // Dispatches through Spring Application Events or RabbitMQ broker
        eventPublisher.publishEvent(message);
    }
}
