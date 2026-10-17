package com.sanket.AI.Code.Review.Platform.ai.dto.response;

import com.sanket.AI.Code.Review.Platform.review.entity.FindingSeverity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiFindingResponse {

    private String issueType;
    private FindingSeverity severity;
    private String fileName;
    private Integer lineNumber;
    private String explanation;
    private String suggestedFix;
    private Double confidenceScore;
    private String summary;
    private String snippetBefore;
    private String snippetAfter;
    private String ruleKey;
}
