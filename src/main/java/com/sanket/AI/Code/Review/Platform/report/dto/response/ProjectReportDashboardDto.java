package com.sanket.AI.Code.Review.Platform.report.dto.response;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectReportDashboardDto {

    private Long projectId;
    private String projectName;
    private long totalReports;
    private long approvedReports;
    private long changesRequestedReports;
    private long rejectedReports;
    private double averageOverallScore;
    private long totalCriticalIssues;
    private long totalHighIssues;

    @Builder.Default
    private List<ReportSummaryCardDto> recentReports = new ArrayList<>();
}
