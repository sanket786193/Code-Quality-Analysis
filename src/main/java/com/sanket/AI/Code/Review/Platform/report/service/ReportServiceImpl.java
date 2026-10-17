package com.sanket.AI.Code.Review.Platform.report.service;

import com.sanket.AI.Code.Review.Platform.exception.UnauthorizedProjectAccessException;
import com.sanket.AI.Code.Review.Platform.project.entity.Project;
import com.sanket.AI.Code.Review.Platform.report.dto.request.ReviewDecisionRequest;
import com.sanket.AI.Code.Review.Platform.report.dto.response.ProjectReportDashboardDto;
import com.sanket.AI.Code.Review.Platform.report.dto.response.ReportResponse;
import com.sanket.AI.Code.Review.Platform.report.dto.response.ReportSummaryCardDto;
import com.sanket.AI.Code.Review.Platform.report.entity.*;
import com.sanket.AI.Code.Review.Platform.report.exception.*;
import com.sanket.AI.Code.Review.Platform.report.html.HtmlReportGenerator;
import com.sanket.AI.Code.Review.Platform.report.mapper.ReportMapper;
import com.sanket.AI.Code.Review.Platform.report.pdf.PdfReportGenerator;
import com.sanket.AI.Code.Review.Platform.report.repository.*;
import com.sanket.AI.Code.Review.Platform.review.entity.FindingSeverity;
import com.sanket.AI.Code.Review.Platform.review.entity.ReviewFinding;
import com.sanket.AI.Code.Review.Platform.review.entity.ReviewJob;
import com.sanket.AI.Code.Review.Platform.review.repository.ReviewJobRepository;
import com.sanket.AI.Code.Review.Platform.security.UserPrincipal;
import com.sanket.AI.Code.Review.Platform.user.entity.User;
import com.sanket.AI.Code.Review.Platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final ReportSummaryRepository reportSummaryRepository;
    private final ReviewInsightRepository reviewInsightRepository;
    private final FileWiseFindingRepository fileWiseFindingRepository;
    private final ComparisonSectionRepository comparisonSectionRepository;
    private final ReviewJobRepository reviewJobRepository;
    private final UserRepository userRepository;
    private final ReportMapper reportMapper;
    private final HtmlReportGenerator htmlReportGenerator;
    private final PdfReportGenerator pdfReportGenerator;
    private final com.sanket.AI.Code.Review.Platform.notification.service.NotificationService notificationService;

    @Override
    @Transactional
    public ReportResponse generateReportFromReviewJob(String jobReference, UserPrincipal currentUser) {
        log.info("Generating review insights report for job reference: {}", jobReference);

        ReviewJob job = reviewJobRepository.findByJobReference(jobReference)
                .orElseThrow(() -> new ReportNotFoundException("Review job not found with reference: " + jobReference));

        validateProjectAccess(job.getProject(), currentUser);

        // Check if report already exists for this job
        reportRepository.findByReviewJobJobReference(jobReference).ifPresent(existing -> {
            log.info("Report already exists for job {}, returning existing report {}", jobReference, existing.getReportReference());
        });

        try {
            // Count findings by severity
            int critical = 0;
            int high = 0;
            int medium = 0;
            int low = 0;

            List<FileWiseFinding> fileFindings = new ArrayList<>();
            List<ComparisonSection> comparisons = new ArrayList<>();

            for (ReviewFinding finding : job.getFindings()) {
                if (finding.getSeverity() == FindingSeverity.CRITICAL) critical++;
                else if (finding.getSeverity() == FindingSeverity.HIGH) high++;
                else if (finding.getSeverity() == FindingSeverity.MEDIUM) medium++;
                else low++;
            }

            // Calculate overall score (deduct points based on severity)
            double calculatedScore = Math.max(0.0, 100.0 - (critical * 25.0 + high * 12.0 + medium * 5.0 + low * 2.0));

            ReviewRecommendation recommendation;
            if (critical > 0 || calculatedScore < 60.0) {
                recommendation = ReviewRecommendation.REJECT;
            } else if (high > 0 || calculatedScore < 85.0) {
                recommendation = ReviewRecommendation.APPROVE_WITH_CHANGES;
            } else {
                recommendation = ReviewRecommendation.APPROVE;
            }

            String reportRef = "RPT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            Report report = Report.builder()
                    .reportReference(reportRef)
                    .reviewJob(job)
                    .project(job.getProject())
                    .pullRequestTitle(job.getPullRequestTitle() != null ? job.getPullRequestTitle() : "Pull Request Review")
                    .authorUsername(job.getSenderUsername())
                    .branchName(job.getBranchName())
                    .commitSha(job.getCommitSha())
                    .status(ReportStatus.GENERATED)
                    .overallScore(calculatedScore)
                    .recommendation(recommendation)
                    .generatedAt(LocalDateTime.now())
                    .build();

            Report savedReport = reportRepository.save(report);

            // Create ReportSummary
            ReportSummary summary = ReportSummary.builder()
                    .report(savedReport)
                    .executiveSummary(job.getSummary() != null ? job.getSummary() : "Automated code governance review.")
                    .totalFiles(job.getFindings().stream().map(ReviewFinding::getFilePath).collect(Collectors.toSet()).size())
                    .criticalCount(critical)
                    .highCount(high)
                    .mediumCount(medium)
                    .lowCount(low)
                    .qualityGateStatus(job.getSonarQualityGateStatus() != null ? job.getSonarQualityGateStatus() : "PASSED")
                    .keyRisks(critical > 0 ? "Critical security/architecture risks detected." : "No critical blockers.")
                    .architectureImpact("Assessed for modular maintainability and standard compliance.")
                    .createdAt(LocalDateTime.now())
                    .build();

            reportSummaryRepository.save(summary);
            savedReport.setSummary(summary);

            // Populate FileWiseFindings and Comparisons
            for (ReviewFinding finding : job.getFindings()) {
                FileWiseFinding fwf = FileWiseFinding.builder()
                        .report(savedReport)
                        .fileName(finding.getFilePath())
                        .lineNumber(finding.getLineNumber() != null ? finding.getLineNumber() : 1)
                        .issueType(finding.getCategory())
                        .severity(finding.getSeverity())
                        .explanation(finding.getDescription())
                        .suggestedFix(finding.getSuggestedFix())
                        .confidenceScore(0.90)
                        .ruleKey(finding.getRuleKey())
                        .build();

                fileWiseFindingRepository.save(fwf);
                fileFindings.add(fwf);

                if (finding.getSuggestedFix() != null && !finding.getSuggestedFix().isBlank()) {
                    ComparisonSection comp = ComparisonSection.builder()
                            .report(savedReport)
                            .fileName(finding.getFilePath())
                            .lineNumber(finding.getLineNumber() != null ? finding.getLineNumber() : 1)
                            .beforeChange("// Code at line " + finding.getLineNumber() + " with " + finding.getSeverity() + " " + finding.getCategory())
                            .afterChange(finding.getDescription())
                            .aiSuggestion(finding.getSuggestedFix())
                            .actualImpact("Improves " + finding.getCategory().toLowerCase() + " and code stability.")
                            .build();

                    comparisonSectionRepository.save(comp);
                    comparisons.add(comp);
                }
            }

            // Generate actionable insights for Team Leads
            List<ReviewInsight> insights = generateInsights(savedReport, critical, high, medium, low);
            reviewInsightRepository.saveAll(insights);
            savedReport.setInsights(insights);
            savedReport.setFileFindings(fileFindings);
            savedReport.setComparisons(comparisons);

            // Dispatch notification to PR author and alert Team Leads
            try {
                notificationService.notifyReviewCompleted(
                        savedReport.getReportReference(),
                        savedReport.getProject().getId(),
                        savedReport.getAuthorUsername(),
                        savedReport.getPullRequestTitle(),
                        savedReport.getOverallScore(),
                        critical,
                        high
                );
                notificationService.notifyDecisionPending(
                        savedReport.getReportReference(),
                        savedReport.getProject().getId(),
                        savedReport.getPullRequestTitle(),
                        savedReport.getAuthorUsername()
                );
            } catch (Exception notifEx) {
                log.warn("Failed to dispatch review completion notification: {}", notifEx.getMessage());
            }

            return reportMapper.toReportResponse(savedReport);

        } catch (Exception ex) {
            log.error("Failed to generate report for job {}: {}", jobReference, ex.getMessage(), ex);
            throw new ReportGenerationException("Failed to generate review report: " + ex.getMessage(), ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ReportResponse getReportById(Long id, UserPrincipal currentUser) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ReportNotFoundException("Report not found with ID: " + id));

        validateProjectAccess(report.getProject(), currentUser);
        return reportMapper.toReportResponse(report);
    }

    @Override
    @Transactional(readOnly = true)
    public ReportResponse getReportByPrId(Long prId, UserPrincipal currentUser) {
        Report report = reportRepository.findFirstByReviewJobPullRequestNumberOrderByGeneratedAtDesc(prId.intValue())
                .orElseThrow(() -> new ReportNotFoundException("Report not found for pull request number: " + prId));

        validateProjectAccess(report.getProject(), currentUser);
        return reportMapper.toReportResponse(report);
    }

    @Override
    @Transactional(readOnly = true)
    public ReportSummaryCardDto getReportSummary(Long id, UserPrincipal currentUser) {
        ReportResponse report = getReportById(id, currentUser);
        return report.getSummaryCard();
    }

    @Override
    @Transactional(readOnly = true)
    public ReportResponse getReportByReference(String reportReference, UserPrincipal currentUser) {
        Report report = reportRepository.findByReportReference(reportReference)
                .orElseThrow(() -> new ReportNotFoundException("Report not found with reference: " + reportReference));

        validateProjectAccess(report.getProject(), currentUser);
        return reportMapper.toReportResponse(report);
    }

    @Override
    @Transactional(readOnly = true)
    public ReportResponse getReportByJobReference(String jobReference, UserPrincipal currentUser) {
        Report report = reportRepository.findByReviewJobJobReference(jobReference)
                .orElseThrow(() -> new ReportNotFoundException("Report not found for job: " + jobReference));

        validateProjectAccess(report.getProject(), currentUser);
        return reportMapper.toReportResponse(report);
    }

    @Override
    @Transactional
    public ReportResponse recordReviewDecision(String reportReference, ReviewDecisionRequest request, UserPrincipal currentUser) {
        if (request.getDecision() == null) {
            throw new InvalidTeamLeadDecisionException("Review decision cannot be null. Allowed: APPROVED, CHANGES_REQUESTED, REJECTED");
        }

        Report report = reportRepository.findByReportReference(reportReference)
                .orElseThrow(() -> new ReportNotFoundException("Report not found with reference: " + reportReference));

        validateTeamLeadOrAdminAccess(report.getProject(), currentUser);

        User lead = userRepository.findById(currentUser.getId()).orElse(null);

        report.setStatus(request.getDecision());
        report.setDecisionComment(request.getComment());
        report.setDecisionBy(lead);
        report.setDecidedAt(LocalDateTime.now());

        Report updated = reportRepository.save(report);
        log.info("Recorded review decision '{}' on report {} by user {}", 
                request.getDecision(), reportReference, currentUser.getUsername());

        // Notify PR author of the Team Lead decision
        try {
            notificationService.notifyDecisionMade(
                    updated.getReportReference(),
                    updated.getProject().getId(),
                    updated.getAuthorUsername(),
                    request.getDecision().name(),
                    request.getComment(),
                    currentUser.getUsername()
            );
        } catch (Exception notifEx) {
            log.warn("Failed to dispatch decision notification: {}", notifEx.getMessage());
        }

        return reportMapper.toReportResponse(updated);
    }

    @Override
    @Transactional
    public ReportResponse recordReviewDecisionById(Long id, ReviewDecisionRequest request, UserPrincipal currentUser) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ReportNotFoundException("Report not found with ID: " + id));
        return recordReviewDecision(report.getReportReference(), request, currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public String generateReportHtml(String reportReference, UserPrincipal currentUser) {
        ReportResponse reportDto = getReportByReference(reportReference, currentUser);
        return htmlReportGenerator.generateHtmlReport(reportDto);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateReportPdf(String reportReference, UserPrincipal currentUser) {
        ReportResponse reportDto = getReportByReference(reportReference, currentUser);
        return pdfReportGenerator.generatePdfReport(reportDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectReportDashboardDto getProjectReportDashboard(Long projectId, UserPrincipal currentUser) {
        long total = reportRepository.countApprovedByProjectId(projectId) 
                   + reportRepository.countChangesRequestedByProjectId(projectId) 
                   + reportRepository.countRejectedByProjectId(projectId);

        long approved = reportRepository.countApprovedByProjectId(projectId);
        long changesReq = reportRepository.countChangesRequestedByProjectId(projectId);
        long rejected = reportRepository.countRejectedByProjectId(projectId);
        Double avgScore = reportRepository.getAverageScoreByProjectId(projectId);

        List<Report> recent = reportRepository.findTop5ByProjectIdOrderByGeneratedAtDesc(projectId);
        List<ReportSummaryCardDto> recentCards = recent.stream()
                .map(r -> reportMapper.toReportResponse(r).getSummaryCard())
                .collect(Collectors.toList());

        return ProjectReportDashboardDto.builder()
                .projectId(projectId)
                .totalReports(total)
                .approvedReports(approved)
                .changesRequestedReports(changesReq)
                .rejectedReports(rejected)
                .averageOverallScore(avgScore != null ? avgScore : 0.0)
                .recentReports(recentCards)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReportResponse> getProjectReports(Long projectId, Pageable pageable, UserPrincipal currentUser) {
        Page<Report> reports = reportRepository.findByProjectId(projectId, pageable);
        return reports.map(reportMapper::toReportResponse);
    }

    private List<ReviewInsight> generateInsights(Report report, int critical, int high, int medium, int low) {
        List<ReviewInsight> list = new ArrayList<>();

        if (critical > 0) {
            list.add(ReviewInsight.builder()
                    .report(report)
                    .category("SECURITY")
                    .impactArea("HIGH")
                    .title("Critical Vulnerability Blocker")
                    .description("Found " + critical + " critical issue(s) that must be remediated prior to merge.")
                    .actionRequired("Mandatory patch required before approval.")
                    .build());
        }

        if (high > 0) {
            list.add(ReviewInsight.builder()
                    .report(report)
                    .category("ARCHITECTURE")
                    .impactArea("MEDIUM")
                    .title("Code Smells & Performance Bottlenecks")
                    .description("Found " + high + " high severity concern(s) impacting code quality.")
                    .actionRequired("Refactor recommended code blocks.")
                    .build());
        }

        list.add(ReviewInsight.builder()
                .report(report)
                .category("GOVERNANCE")
                .impactArea("LOW")
                .title("Standard Compliance Verification")
                .description(String.format("Review scored %.1f/100 across security and style conventions.", report.getOverallScore()))
                .actionRequired("Team Lead sign-off.")
                .build());

        return list;
    }

    private void validateProjectAccess(Project project, UserPrincipal currentUser) {
        if (currentUser == null) throw new UnauthorizedProjectAccessException("Authentication required.");
        if (hasRole(currentUser, "ROLE_ADMIN")) return;
        if (project.getOwner() != null && project.getOwner().getId().equals(currentUser.getId())) return;
        if (project.getTeamLead() != null && project.getTeamLead().getId().equals(currentUser.getId())) return;

        throw new UnauthorizedProjectAccessException("You do not have permission to view reports for project '" + project.getName() + "'.");
    }

    private void validateTeamLeadOrAdminAccess(Project project, UserPrincipal currentUser) {
        if (currentUser == null) throw new UnauthorizedProjectAccessException("Authentication required.");
        if (hasRole(currentUser, "ROLE_ADMIN")) return;
        if (hasRole(currentUser, "ROLE_TEAM_LEAD") && project.getTeamLead() != null && project.getTeamLead().getId().equals(currentUser.getId())) {
            return;
        }
        throw new UnauthorizedProjectAccessException("Only assigned Team Leads or Admins can record review decisions.");
    }

    private boolean hasRole(UserPrincipal user, String roleName) {
        if (user == null || user.getAuthorities() == null) return false;
        return user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals(roleName));
    }
}
