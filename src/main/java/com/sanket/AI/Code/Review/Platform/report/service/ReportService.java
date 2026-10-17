package com.sanket.AI.Code.Review.Platform.report.service;

import com.sanket.AI.Code.Review.Platform.report.dto.request.ReviewDecisionRequest;
import com.sanket.AI.Code.Review.Platform.report.dto.response.ProjectReportDashboardDto;
import com.sanket.AI.Code.Review.Platform.report.dto.response.ReportResponse;
import com.sanket.AI.Code.Review.Platform.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReportService {

    /**
     * Generates and stores a complete review report and insights from an analyzed ReviewJob.
     */
    ReportResponse generateReportFromReviewJob(String jobReference, UserPrincipal currentUser);

    /**
     * Retrieves full report by unique report reference with role-based project access control.
     */
    ReportResponse getReportByReference(String reportReference, UserPrincipal currentUser);

    /**
     * Retrieves report by underlying review job reference.
     */
    ReportResponse getReportByJobReference(String jobReference, UserPrincipal currentUser);

    /**
     * Allows Team Leads or Admins to record a governance review decision (Approve, Request Changes, Reject).
     */
    ReportResponse recordReviewDecision(String reportReference, ReviewDecisionRequest request, UserPrincipal currentUser);

    /**
     * Generates a modern HTML report view for browser rendering.
     */
    String generateReportHtml(String reportReference, UserPrincipal currentUser);

    /**
     * Generates a downloadable PDF binary document.
     */
    byte[] generateReportPdf(String reportReference, UserPrincipal currentUser);

    /**
     * Retrieves dashboard summary metrics for a project.
     */
    ProjectReportDashboardDto getProjectReportDashboard(Long projectId, UserPrincipal currentUser);

    /**
     * Retrieves report by numeric primary key ID.
     */
    ReportResponse getReportById(Long id, UserPrincipal currentUser);

    /**
     * Retrieves report by pull request number or ID.
     */
    ReportResponse getReportByPrId(Long prId, UserPrincipal currentUser);

    /**
     * Retrieves summary card and quality gate score by report ID.
     */
    ReportSummaryCardDto getReportSummary(Long id, UserPrincipal currentUser);

    /**
     * Allows Team Leads or Admins to record a governance review decision by report numeric ID.
     */
    ReportResponse recordReviewDecisionById(Long id, ReviewDecisionRequest request, UserPrincipal currentUser);

    /**
     * Retrieves paginated reports for a project.
     */
    Page<ReportResponse> getProjectReports(Long projectId, Pageable pageable, UserPrincipal currentUser);
}
