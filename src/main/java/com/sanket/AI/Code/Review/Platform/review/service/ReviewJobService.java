package com.sanket.AI.Code.Review.Platform.review.service;

import com.sanket.AI.Code.Review.Platform.project.entity.RepositoryMapping;
import com.sanket.AI.Code.Review.Platform.review.dto.request.CreateManualReviewRequest;
import com.sanket.AI.Code.Review.Platform.review.dto.response.ReviewInsightsResponse;
import com.sanket.AI.Code.Review.Platform.review.dto.response.ReviewJobDetailResponse;
import com.sanket.AI.Code.Review.Platform.review.dto.response.ReviewJobResponse;
import com.sanket.AI.Code.Review.Platform.review.entity.ReviewEventType;
import com.sanket.AI.Code.Review.Platform.review.entity.ReviewJob;
import com.sanket.AI.Code.Review.Platform.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

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
     * Creates a manual review job on demand for a project and branch.
     */
    ReviewJobResponse createManualReviewJob(Long projectId, CreateManualReviewRequest request, UserPrincipal currentUser);

    /**
     * Executes the complete review pipeline (Diff parsing, SonarQube, Rule Engine, and AI context assembly).
     */
    void executeReviewPipeline(Long reviewJobId);

    /**
     * Retrieves paginated review jobs for a project.
     */
    Page<ReviewJobResponse> getProjectReviewJobs(Long projectId, Pageable pageable, UserPrincipal currentUser);

    /**
     * Retrieves review history for a specific pull request number.
     */
    List<ReviewJobResponse> getPullRequestReviews(Long projectId, Integer prNumber, UserPrincipal currentUser);

    /**
     * Retrieves review jobs submitted by the current authenticated developer.
     */
    Page<ReviewJobResponse> getDeveloperReviews(UserPrincipal currentUser, Pageable pageable);

    /**
     * Retrieves full review job report with findings and summary.
     */
    ReviewJobDetailResponse getReviewJobDetails(String jobReference, UserPrincipal currentUser);

    /**
     * Retrieves aggregated review insights for a project (Team Lead/Admin dashboard).
     */
    ReviewInsightsResponse getProjectReviewInsights(Long projectId, UserPrincipal currentUser);
}
