package com.sanket.AI.Code.Review.Platform.report.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResponse {

    private Long reportId;
    private String reportReference;
    private String jobReference;
    private Long projectId;
    private String projectName;

    // 1. Summary Card
    private ReportSummaryCardDto summaryCard;

    // 2. Issue Breakdown
    private IssueBreakdownDto issueBreakdown;

    // 3. AI Recommendation
    private AiRecommendationDto recommendation;

    // 4. File-Wise View
    @Builder.Default
    private List<FileWiseFindingDto> fileWiseFindings = new ArrayList<>();

    // 5. Comparison Section
    @Builder.Default
    private List<ComparisonSectionDto> comparisonSections = new ArrayList<>();

    // Actionable Insights for Team Leads
    @Builder.Default
    private List<ReviewInsightDto> insights = new ArrayList<>();

    private String executiveSummary;
    private String decisionComment;
    private String decisionByUsername;
    private LocalDateTime decidedAt;
    private LocalDateTime generatedAt;
}
