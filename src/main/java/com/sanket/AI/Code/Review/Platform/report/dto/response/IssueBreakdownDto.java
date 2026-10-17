package com.sanket.AI.Code.Review.Platform.report.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueBreakdownDto {

    private int critical;
    private int high;
    private int medium;
    private int low;
    private int total;
    private String qualityGateStatus;
}
