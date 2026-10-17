package com.sanket.AI.Code.Review.Platform.report.dto.response;

import com.sanket.AI.Code.Review.Platform.report.entity.ReviewRecommendation;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiRecommendationDto {

    private ReviewRecommendation recommendation;
    private String rationale;
    private Double confidenceScore;
}
