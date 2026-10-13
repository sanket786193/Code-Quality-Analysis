package com.sanket.AI.Code.Review.Platform.review.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewInsightsResponse {

    private Long projectId;
    private String projectName;
    private long totalReviews;
    private long completedReviews;
    private long failedReviews;
    private long criticalFindingsTotal;
    private long highFindingsTotal;
    private long mediumFindingsTotal;
    private double qualityGatePassRate;
    private List<ReviewJobResponse> recentReviews;
}
