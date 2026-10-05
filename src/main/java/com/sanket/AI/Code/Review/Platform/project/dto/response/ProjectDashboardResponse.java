package com.sanket.AI.Code.Review.Platform.project.dto.response;

import com.sanket.AI.Code.Review.Platform.review.dto.response.ReviewJobResponse;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDashboardResponse {
    private ProjectResponse project;
    private long totalReviews;
    private long queuedReviews;
    private long processingReviews;
    private long completedReviews;
    private long failedReviews;
    private long criticalFindingsCount;
    private List<ReviewJobResponse> recentReviewJobs;
}
