package com.sanket.AI.Code.Review.Platform.review.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewJobDetailResponse {
    private ReviewJobResponse job;
    private List<ReviewFindingResponse> findings;
}
