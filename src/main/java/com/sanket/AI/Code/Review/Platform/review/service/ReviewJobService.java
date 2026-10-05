package com.sanket.AI.Code.Review.Platform.review.service;

import com.sanket.AI.Code.Review.Platform.project.entity.RepositoryMapping;
import com.sanket.AI.Code.Review.Platform.review.dto.response.ReviewJobDetailResponse;
import com.sanket.AI.Code.Review.Platform.review.dto.response.ReviewJobResponse;
import com.sanket.AI.Code.Review.Platform.review.entity.ReviewEventType;
import com.sanket.AI.Code.Review.Platform.review.entity.ReviewJob;
import com.sanket.AI.Code.Review.Platform.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewJobService {

    /**
     * Creates and enqueues a new review job from a verified GitHub webhook event.
     */
    ReviewJob createReviewJobFromWebhook(
            RepositoryMapping mapping,
            ReviewEventType eventType,
            String commitSha,
            Integer pullRequestNumber,
            String pullRequestTitle,
            String branchName,
            String senderUsername
    );

    /**
     * Dispatches the review job to the asynchronous worker queue for AI processing.
     */
    void dispatchJobToQueue(Long reviewJobId);

    /**
     * Retrieves paginated review jobs for a project.
     */
    Page<ReviewJobResponse> getProjectReviewJobs(Long projectId, Pageable pageable, UserPrincipal currentUser);

    /**
     * Retrieves review job details with all findings.
     */
    ReviewJobDetailResponse getReviewJobDetails(String jobReference, UserPrincipal currentUser);
}
