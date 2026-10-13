package com.sanket.AI.Code.Review.Platform.ai.model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiCodeReviewResult {

    private String summary;
    private Double overallScore; // 0.0 - 100.0
    private AiReviewRecommendation recommendation;
    private Double confidenceScore;
    private String rawOutput;
    private Long latencyMs;
    private String modelUsed;

    @Builder.Default
    private List<AiReviewFindingModel> findings = new ArrayList<>();
}
