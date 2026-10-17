package com.sanket.AI.Code.Review.Platform.ruleengine.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleEvaluationResponse {

    private String fileName;
    private int totalViolations;
    private boolean passed;
    private int criticalCount;
    private int highCount;
    private int mediumCount;
    private int lowCount;
    private List<RuleViolationDto> violations;
    private String summary;
}
