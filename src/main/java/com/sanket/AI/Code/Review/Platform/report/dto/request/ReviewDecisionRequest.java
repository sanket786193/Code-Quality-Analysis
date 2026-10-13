package com.sanket.AI.Code.Review.Platform.report.dto.request;

import com.sanket.AI.Code.Review.Platform.report.entity.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDecisionRequest {

    @NotNull(message = "Decision status is required (APPROVED, CHANGES_REQUESTED, REJECTED)")
    private ReportStatus decision;

    private String comment;
}
