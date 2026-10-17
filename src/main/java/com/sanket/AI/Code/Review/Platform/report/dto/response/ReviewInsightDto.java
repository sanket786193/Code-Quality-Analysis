package com.sanket.AI.Code.Review.Platform.report.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewInsightDto {

    private String category;
    private String impactArea;
    private String title;
    private String description;
    private String actionRequired;
}
