package com.sanket.AI.Code.Review.Platform.report.mapper;

import com.sanket.AI.Code.Review.Platform.report.dto.response.*;
import com.sanket.AI.Code.Review.Platform.report.entity.*;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReportMapper {

    public ReportResponse toReportResponse(Report report) {
        if (report == null) return null;

        ReportSummary summary = report.getSummary();

        // 1. Summary Card
        ReportSummaryCardDto summaryCard = ReportSummaryCardDto.builder()
                .pullRequestTitle(report.getPullRequestTitle())
                .author(report.getAuthorUsername())
                .branch(report.getBranchName())
                .targetBranch(report.getTargetBranch())
                .commitSha(report.getCommitSha())
                .status(report.getStatus())
                .overallScore(report.getOverallScore())
                .build();

        // 2. Issue Breakdown
        IssueBreakdownDto issueBreakdown = IssueBreakdownDto.builder()
                .critical(summary != null ? summary.getCriticalCount() : 0)
                .high(summary != null ? summary.getHighCount() : 0)
                .medium(summary != null ? summary.getMediumCount() : 0)
                .low(summary != null ? summary.getLowCount() : 0)
                .total(summary != null ? (summary.getCriticalCount() + summary.getHighCount() + summary.getMediumCount() + summary.getLowCount()) : 0)
                .qualityGateStatus(summary != null ? summary.getQualityGateStatus() : "PASSED")
                .build();

        // 3. AI Recommendation
        AiRecommendationDto recommendation = AiRecommendationDto.builder()
                .recommendation(report.getRecommendation())
                .rationale(summary != null ? summary.getExecutiveSummary() : "Automated review evaluation")
                .confidenceScore(0.92)
                .build();

        // 4. File-Wise Findings
        List<FileWiseFindingDto> fileFindings = report.getFileFindings() != null
                ? report.getFileFindings().stream().map(this::toFileFindingDto).collect(Collectors.toList())
                : Collections.emptyList();

        // 5. Comparison Section
        List<ComparisonSectionDto> comparisons = report.getComparisons() != null
                ? report.getComparisons().stream().map(this::toComparisonDto).collect(Collectors.toList())
                : Collections.emptyList();

        // Insights
        List<ReviewInsightDto> insights = report.getInsights() != null
                ? report.getInsights().stream().map(this::toInsightDto).collect(Collectors.toList())
                : Collections.emptyList();

        return ReportResponse.builder()
                .reportId(report.getId())
                .reportReference(report.getReportReference())
                .jobReference(report.getReviewJob() != null ? report.getReviewJob().getJobReference() : null)
                .projectId(report.getProject() != null ? report.getProject().getId() : null)
                .projectName(report.getProject() != null ? report.getProject().getName() : null)
                .summaryCard(summaryCard)
                .issueBreakdown(issueBreakdown)
                .recommendation(recommendation)
                .fileWiseFindings(fileFindings)
                .comparisonSections(comparisons)
                .insights(insights)
                .executiveSummary(summary != null ? summary.getExecutiveSummary() : null)
                .decisionComment(report.getDecisionComment())
                .decisionByUsername(report.getDecisionBy() != null ? report.getDecisionBy().getUsername() : null)
                .decidedAt(report.getDecidedAt())
                .generatedAt(report.getGeneratedAt())
                .build();
    }

    public FileWiseFindingDto toFileFindingDto(FileWiseFinding f) {
        if (f == null) return null;
        return FileWiseFindingDto.builder()
                .fileName(f.getFileName())
                .lineNumber(f.getLineNumber())
                .issueType(f.getIssueType())
                .severity(f.getSeverity())
                .explanation(f.getExplanation())
                .suggestion(f.getSuggestedFix())
                .confidenceScore(f.getConfidenceScore())
                .ruleKey(f.getRuleKey())
                .build();
    }

    public ComparisonSectionDto toComparisonDto(ComparisonSection c) {
        if (c == null) return null;
        return ComparisonSectionDto.builder()
                .fileName(c.getFileName())
                .lineNumber(c.getLineNumber())
                .beforeChange(c.getBeforeChange())
                .afterChange(c.getAfterChange())
                .aiSuggestion(c.getAiSuggestion())
                .actualImpact(c.getActualImpact())
                .build();
    }

    public ReviewInsightDto toInsightDto(ReviewInsight i) {
        if (i == null) return null;
        return ReviewInsightDto.builder()
                .category(i.getCategory())
                .impactArea(i.getImpactArea())
                .title(i.getTitle())
                .description(i.getDescription())
                .actionRequired(i.getActionRequired())
                .build();
    }
}
