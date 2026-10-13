package com.sanket.AI.Code.Review.Platform.review.rabbitmq.consumer;

import com.sanket.AI.Code.Review.Platform.review.rabbitmq.message.ReviewJobMessage;
import com.sanket.AI.Code.Review.Platform.review.service.ReviewJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewJobConsumer {

    private final ReviewJobService reviewJobService;

    @Async("reviewJobExecutor")
    @EventListener
    public void consumeReviewJob(ReviewJobMessage message) {
        log.info("Dequeued review job reference: {} for processing", message.getJobReference());
        reviewJobService.executeReviewPipeline(message.getReviewJobId());
    }
}
