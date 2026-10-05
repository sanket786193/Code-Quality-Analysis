package com.sanket.AI.Code.Review.Platform.review.controller;

import com.sanket.AI.Code.Review.Platform.review.dto.response.ReviewJobDetailResponse;
import com.sanket.AI.Code.Review.Platform.review.dto.response.ReviewJobResponse;
import com.sanket.AI.Code.Review.Platform.review.service.ReviewJobService;
import com.sanket.AI.Code.Review.Platform.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Review Jobs", description = "Endpoints for tracking AI code review jobs and findings")
@SecurityRequirement(name = "bearerAuth")
public class ReviewJobController {

    private final ReviewJobService reviewJobService;

    @GetMapping("/projects/{projectId}/reviews")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'TEAM_LEAD', 'ADMIN')")
    @Operation(summary = "List Project Reviews", description = "Retrieves paginated review jobs for a specified project")
    public ResponseEntity<Page<ReviewJobResponse>> getProjectReviewJobs(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PageableDefault(size = 10, sort = "queuedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ReviewJobResponse> page = reviewJobService.getProjectReviewJobs(projectId, pageable, currentUser);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/reviews/{jobReference}")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'TEAM_LEAD', 'ADMIN')")
    @Operation(summary = "Get Review Details", description = "Retrieves complete review job report including all AI findings")
    public ResponseEntity<ReviewJobDetailResponse> getReviewJobDetails(
            @PathVariable String jobReference,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ReviewJobDetailResponse response = reviewJobService.getReviewJobDetails(jobReference, currentUser);
        return ResponseEntity.ok(response);
    }
}
