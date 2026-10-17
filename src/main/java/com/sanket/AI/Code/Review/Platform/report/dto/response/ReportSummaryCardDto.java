package com.sanket.AI.Code.Review.Platform.report.dto.response;

import com.sanket.AI.Code.Review.Platform.report.entity.ReportStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportSummaryCardDto {

    private String pullRequestTitle;
    private String author;
    private String branch;
    private String targetBranch;
    private String commitSha;
    private ReportStatus status;
    private Double overallScore;
}
