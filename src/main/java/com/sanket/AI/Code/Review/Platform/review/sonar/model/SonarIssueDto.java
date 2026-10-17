package com.sanket.AI.Code.Review.Platform.review.sonar.model;

import com.sanket.AI.Code.Review.Platform.review.entity.FindingSeverity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SonarIssueDto {

    private String key;
    private String rule;
    private FindingSeverity severity;
    private String component; // file path
    private Integer line;
    private String message;
    private String type; // BUG, VULNERABILITY, CODE_SMELL, SECURITY_HOTSPOT
    private String effort;
}
