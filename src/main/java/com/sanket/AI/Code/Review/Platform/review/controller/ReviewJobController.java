package com.sanket.AI.Code.Review.Platform.review.controller;

import com.sanket.AI.Code.Review.Platform.review.dto.request.CreateManualReviewRequest;
import com.sanket.AI.Code.Review.Platform.review.dto.response.ReviewInsightsResponse;
import com.sanket.AI.Code.Review.Platform.review.dto.response.ReviewJobDetailResponse;
import com.sanket.AI.Code.Review.Platform.review.dto.response.ReviewJobResponse;
import com.sanket.AI.Code.Review.Platform.review.service.ReviewJobService;
import com.sanket.AI.Code.Review.Platform.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Review Jobs & Insights", description = "Endpoints for managing code reviews, SonarQube results, rule violations, and team insights")
@SecurityRequirement(name = "bearerAuth")
public class ReviewJobController {

    private final ReviewJobService reviewJobService;

    @PostMapping("/projects/{projectId}/reviews")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN')")
    @Operation(summary = "Trigger Manual Review", description = "Triggers an on-demand code review for a project and branch")
    public ResponseEntity<ReviewJobResponse> triggerManualReview(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateManualReviewRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ReviewJobResponse response = reviewJobService.createManualReviewJob(projectId, request, currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/projects/{projectId}/reviews")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'TEAM_LEAD', 'ADMIN')")
    @Operation(summary = "List Project Reviews", description = "Retrieves paginated review history for a specified project")
    public ResponseEntity<Page<ReviewJobResponse>> getProjectReviewJobs(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PageableDefault(size = 10, sort = "queuedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ReviewJobResponse> page = reviewJobService.getProjectReviewJobs(projectId, pageable, currentUser);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/projects/{projectId}/reviews/pr/{prNumber}")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'TEAM_LEAD', 'ADMIN')")
    @Operation(summary = "Get PR Review History", description = "Retrieves review history for a specific pull request number")
    public ResponseEntity<List<ReviewJobResponse>> getPullRequestReviews(
            @PathVariable Long projectId,
            @PathVariable Integer prNumber,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        List<ReviewJobResponse> reviews = reviewJobService.getPullRequestReviews(projectId, prNumber, currentUser);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/reviews/my-reviews")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'TEAM_LEAD', 'ADMIN')")
    @Operation(summary = "Get My Reviews (Developer)", description = "Retrieves reviews initiated by or associated with the logged-in user")
    public ResponseEntity<Page<ReviewJobResponse>> getMyReviews(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PageableDefault(size = 10, sort = "queuedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ReviewJobResponse> page = reviewJobService.getDeveloperReviews(currentUser, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/reviews/{jobReference}")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'TEAM_LEAD', 'ADMIN')")
    @Operation(summary = "Get Review Details", description = "Retrieves complete review report including SonarQube, Rule Engine findings, and prepared AI context")
    public ResponseEntity<ReviewJobDetailResponse> getReviewJobDetails(
            @PathVariable String jobReference,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ReviewJobDetailResponse response = reviewJobService.getReviewJobDetails(jobReference, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/projects/{projectId}/insights")
    @PreAuthorize("hasAnyRole('TEAM_LEAD', 'ADMIN')")
    @Operation(summary = "Get Project Review Insights (Team Lead/Admin)", description = "Retrieves aggregated review metrics, pass rate, and recent audits for governance")
    public ResponseEntity<ReviewInsightsResponse> getProjectReviewInsights(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ReviewInsightsResponse response = reviewJobService.getProjectReviewInsights(projectId, currentUser);
        return ResponseEntity.ok(response);
    }
}
