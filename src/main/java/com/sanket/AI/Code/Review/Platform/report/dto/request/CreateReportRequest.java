package com.sanket.AI.Code.Review.Platform.report.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReportRequest {

    @NotBlank(message = "Job reference is required to generate report")
    private String jobReference;

    private Long reviewJobId;
}
