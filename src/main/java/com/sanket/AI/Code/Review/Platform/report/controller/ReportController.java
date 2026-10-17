package com.sanket.AI.Code.Review.Platform.report.controller;

import com.sanket.AI.Code.Review.Platform.common.dto.ApiResponse;
import com.sanket.AI.Code.Review.Platform.report.dto.request.ReviewDecisionRequest;
import com.sanket.AI.Code.Review.Platform.report.dto.response.ProjectReportDashboardDto;
import com.sanket.AI.Code.Review.Platform.report.dto.response.ReportResponse;
import com.sanket.AI.Code.Review.Platform.report.service.ReportService;
import com.sanket.AI.Code.Review.Platform.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/reports", "/api/v1/reports"})
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Review Insights & Reports", description = "Endpoints for generating, viewing, and exporting review reports and insights")
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/generate/{jobReference}")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'TEAM_LEAD', 'ADMIN')")
    @Operation(summary = "Generate Review Report", description = "Generates and persists human-readable report from review job findings")
    public ResponseEntity<ApiResponse<ReportResponse>> generateReport(
            @PathVariable String jobReference,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ReportResponse response = reportService.generateReportFromReviewJob(jobReference, currentUser);
        return ResponseEntity.ok(ApiResponse.success(response, "Review report generated successfully"));
    }

    @GetMapping("/{idOrReference}")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'TEAM_LEAD', 'ADMIN')")
    @Operation(summary = "Get Report Details", description = "Retrieves full report by numeric ID or report reference (e.g., RPT-...)")
    public ResponseEntity<ApiResponse<ReportResponse>> getReport(
            @PathVariable String idOrReference,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ReportResponse response;
        if (idOrReference.matches("^\\d+$")) {
            response = reportService.getReportById(Long.parseLong(idOrReference), currentUser);
        } else {
            response = reportService.getReportByReference(idOrReference, currentUser);
        }
        return ResponseEntity.ok(ApiResponse.success(response, "Report retrieved successfully"));
    }

    @GetMapping("/{idOrReference}/summary")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'TEAM_LEAD', 'ADMIN')")
    @Operation(summary = "Get Report Summary Card", description = "Retrieves score and high-level summary card for a report")
    public ResponseEntity<ApiResponse<com.sanket.AI.Code.Review.Platform.report.dto.response.ReportSummaryCardDto>> getReportSummary(
            @PathVariable String idOrReference,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ReportResponse response;
        if (idOrReference.matches("^\\d+$")) {
            response = reportService.getReportById(Long.parseLong(idOrReference), currentUser);
        } else {
            response = reportService.getReportByReference(idOrReference, currentUser);
        }
        return ResponseEntity.ok(ApiResponse.success(response.getSummaryCard(), "Report summary retrieved successfully"));
    }

    @GetMapping("/pr/{prId}")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'TEAM_LEAD', 'ADMIN')")
    @Operation(summary = "Get Report By Pull Request ID/Number", description = "Retrieves report corresponding to pull request ID/number")
    public ResponseEntity<ApiResponse<ReportResponse>> getReportByPr(
            @PathVariable Long prId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ReportResponse response = reportService.getReportByPrId(prId, currentUser);
        return ResponseEntity.ok(ApiResponse.success(response, "Report for PR retrieved successfully"));
    }

    @GetMapping("/job/{jobReference}")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'TEAM_LEAD', 'ADMIN')")
    @Operation(summary = "Get Report By Review Job", description = "Retrieves report corresponding to a review job reference")
    public ResponseEntity<ApiResponse<ReportResponse>> getReportByJob(
            @PathVariable String jobReference,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ReportResponse response = reportService.getReportByJobReference(jobReference, currentUser);
        return ResponseEntity.ok(ApiResponse.success(response, "Report retrieved successfully"));
    }

    @PostMapping("/{idOrReference}/decision")
    @PreAuthorize("hasAnyRole('TEAM_LEAD', 'ADMIN')")
    @Operation(summary = "Record Review Decision", description = "Allows Team Leads and Admins to approve, request changes, or reject the review")
    public ResponseEntity<ApiResponse<ReportResponse>> recordDecision(
            @PathVariable String idOrReference,
            @Valid @RequestBody ReviewDecisionRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ReportResponse response;
        if (idOrReference.matches("^\\d+$")) {
            response = reportService.recordReviewDecisionById(Long.parseLong(idOrReference), request, currentUser);
        } else {
            response = reportService.recordReviewDecision(idOrReference, request, currentUser);
        }
        return ResponseEntity.ok(ApiResponse.success(response, "Review decision recorded successfully"));
    }

    @GetMapping(value = "/{reportReference}/download/html", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasAnyRole('DEVELOPER', 'TEAM_LEAD', 'ADMIN')")
    @Operation(summary = "Render HTML Report", description = "Returns responsive HTML report view")
    public ResponseEntity<String> downloadHtmlReport(
            @PathVariable String reportReference,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        String htmlContent = reportService.generateReportHtml(reportReference, currentUser);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"report-" + reportReference + ".html\"")
                .body(htmlContent);
    }

    @GetMapping(value = "/{reportReference}/download/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('DEVELOPER', 'TEAM_LEAD', 'ADMIN')")
    @Operation(summary = "Download PDF Report", description = "Generates and downloads binary PDF report")
    public ResponseEntity<byte[]> downloadPdfReport(
            @PathVariable String reportReference,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        byte[] pdfBytes = reportService.generateReportPdf(reportReference, currentUser);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report-" + reportReference + ".pdf\"")
                .body(pdfBytes);
    }

    @GetMapping("/project/{projectId}/dashboard")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'TEAM_LEAD', 'ADMIN')")
    @Operation(summary = "Project Review Dashboard", description = "Retrieves aggregated statistics and scores for a project dashboard")
    public ResponseEntity<ApiResponse<ProjectReportDashboardDto>> getProjectDashboard(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ProjectReportDashboardDto dashboard = reportService.getProjectReportDashboard(projectId, currentUser);
        return ResponseEntity.ok(ApiResponse.success(dashboard, "Project review dashboard retrieved successfully"));
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'TEAM_LEAD', 'ADMIN')")
    @Operation(summary = "List Project Reports", description = "Retrieves paginated reports for a project")
    public ResponseEntity<ApiResponse<Page<ReportResponse>>> getProjectReports(
            @PathVariable Long projectId,
            @PageableDefault(size = 10, sort = "generatedAt") Pageable pageable,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        Page<ReportResponse> page = reportService.getProjectReports(projectId, pageable, currentUser);
        return ResponseEntity.ok(ApiResponse.success(page, "Project reports retrieved successfully"));
    }
}
