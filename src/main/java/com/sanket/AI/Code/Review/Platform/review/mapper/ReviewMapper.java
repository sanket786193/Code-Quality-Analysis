package com.sanket.AI.Code.Review.Platform.review.mapper;

import com.sanket.AI.Code.Review.Platform.review.dto.response.ReviewFindingResponse;
import com.sanket.AI.Code.Review.Platform.review.dto.response.ReviewJobDetailResponse;
import com.sanket.AI.Code.Review.Platform.review.dto.response.ReviewJobResponse;
import com.sanket.AI.Code.Review.Platform.review.dto.response.ReviewSummaryResponse;
import com.sanket.AI.Code.Review.Platform.review.entity.ReviewFinding;
import com.sanket.AI.Code.Review.Platform.review.entity.ReviewJob;
import com.sanket.AI.Code.Review.Platform.review.entity.ReviewSummary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReviewMapper {

    public ReviewJobResponse toReviewJobResponse(ReviewJob job) {
        if (job == null) return null;

        return ReviewJobResponse.builder()
                .id(job.getId())
                .jobReference(job.getJobReference())
                .projectId(job.getProject() != null ? job.getProject().getId() : null)
                .projectName(job.getProject() != null ? job.getProject().getName() : null)
                .repoFullName(job.getRepositoryMapping() != null ? job.getRepositoryMapping().getRepoFullName() : null)
                .eventType(job.getEventType())
                .status(job.getStatus())
                .commitSha(job.getCommitSha())
                .pullRequestNumber(job.getPullRequestNumber())
                .pullRequestTitle(job.getPullRequestTitle())
                .branchName(job.getBranchName())
                .senderUsername(job.getSenderUsername())
                .findingsCount(job.getFindingsCount())
                .sonarQualityGateStatus(job.getSonarQualityGateStatus())
                .summary(job.getSummary())
                .queuedAt(job.getQueuedAt())
                .startedAt(job.getStartedAt())
                .completedAt(job.getCompletedAt())
                .build();
    }

    public ReviewFindingResponse toReviewFindingResponse(ReviewFinding finding) {
        if (finding == null) return null;

        return ReviewFindingResponse.builder()
                .id(finding.getId())
                .filePath(finding.getFilePath())
                .lineNumber(finding.getLineNumber())
                .severity(finding.getSeverity())
                .category(finding.getCategory())
                .title(finding.getTitle())
                .description(finding.getDescription())
                .suggestedFix(finding.getSuggestedFix())
                .ruleKey(finding.getRuleKey())
                .ruleName(finding.getRuleName())
                .source(finding.getSource())
                .createdAt(finding.getCreatedAt())
                .build();
    }

    public ReviewSummaryResponse toReviewSummaryResponse(ReviewSummary summary) {
        if (summary == null) return null;

        return ReviewSummaryResponse.builder()
                .id(summary.getId())
                .totalFilesAnalyzed(summary.getTotalFilesAnalyzed())
                .criticalCount(summary.getCriticalCount())
                .highCount(summary.getHighCount())
                .mediumCount(summary.getMediumCount())
                .lowCount(summary.getLowCount())
                .sonarQualityGateStatus(summary.getSonarQualityGateStatus())
                .executiveSummary(summary.getExecutiveSummary())
                .actionableAdvice(summary.getActionableAdvice())
                .createdAt(summary.getCreatedAt())
                .build();
    }

    public ReviewJobDetailResponse toReviewJobDetailResponse(ReviewJob job, List<ReviewFinding> findings, ReviewSummary summary) {
        return ReviewJobDetailResponse.builder()
                .job(toReviewJobResponse(job))
                .summary(toReviewSummaryResponse(summary))
                .findings(findings != null ? findings.stream().map(this::toReviewFindingResponse).collect(Collectors.toList()) : List.of())
                .aiReviewContext(job != null ? job.getAiReviewContext() : null)
                .build();
    }
}
