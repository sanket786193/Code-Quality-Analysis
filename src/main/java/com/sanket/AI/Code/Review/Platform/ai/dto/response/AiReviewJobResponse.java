package com.sanket.AI.Code.Review.Platform.ai.dto.response;

import com.sanket.AI.Code.Review.Platform.ai.model.AiReviewRecommendation;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiReviewJobResponse {

    private String jobReference;
    private Long reviewJobId;
    private AiReviewRecommendation recommendation;
    private Double overallScore;
    private String summary;
    private Double confidenceScore;
    private Long latencyMs;
    private String modelUsed;

    @Builder.Default
    private List<AiFindingResponse> findings = new ArrayList<>();

    @Builder.Default
    private LocalDateTime reviewedAt = LocalDateTime.now();
}
