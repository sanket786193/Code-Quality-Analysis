package com.sanket.AI.Code.Review.Platform.report.dto.response;

import com.sanket.AI.Code.Review.Platform.review.entity.FindingSeverity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileWiseFindingDto {

    private String fileName;
    private Integer lineNumber;
    private String issueType;
    private FindingSeverity severity;
    private String explanation;
    private String suggestion;
    private Double confidenceScore;
    private String ruleKey;
}
