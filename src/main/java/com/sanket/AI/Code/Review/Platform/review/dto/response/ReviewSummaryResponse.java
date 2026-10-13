package com.sanket.AI.Code.Review.Platform.review.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewSummaryResponse {

    private Long id;
    private Integer totalFilesAnalyzed;
    private Integer criticalCount;
    private Integer highCount;
    private Integer mediumCount;
    private Integer lowCount;
    private String sonarQualityGateStatus;
    private String executiveSummary;
    private String actionableAdvice;
    private LocalDateTime createdAt;
}
