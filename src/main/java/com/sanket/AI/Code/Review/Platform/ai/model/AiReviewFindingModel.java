package com.sanket.AI.Code.Review.Platform.ai.model;

import com.sanket.AI.Code.Review.Platform.review.entity.FindingSeverity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiReviewFindingModel {

    private String issueType; // e.g. SECURITY, PERFORMANCE, CODE_SMELL, BUG, ARCHITECTURE
    private FindingSeverity severity;
    private String fileName;
    private Integer lineNumber;
    private String explanation;
    private String suggestedFix;
    private Double confidenceScore; // 0.0 - 1.0
    private String summary;
    private String snippetBefore;
    private String snippetAfter;
    private String ruleKey;
}
