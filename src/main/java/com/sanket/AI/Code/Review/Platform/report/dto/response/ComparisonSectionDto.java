package com.sanket.AI.Code.Review.Platform.report.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComparisonSectionDto {

    private String fileName;
    private Integer lineNumber;
    private String beforeChange;
    private String afterChange;
    private String aiSuggestion;
    private String actualImpact;
}
