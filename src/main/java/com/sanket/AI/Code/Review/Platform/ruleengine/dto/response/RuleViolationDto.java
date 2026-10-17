package com.sanket.AI.Code.Review.Platform.ruleengine.dto.response;

import com.sanket.AI.Code.Review.Platform.ruleengine.entity.RuleCategory;
import com.sanket.AI.Code.Review.Platform.ruleengine.entity.RuleSeverity;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleViolationDto {

    private Long id;
    private String ruleKey;
    private String ruleName;
    private RuleCategory category;
    private String categoryDisplayName;
    private RuleSeverity severity;
    private String filePath;
    private Integer lineNumber;
    private String matchedSnippet;
    private String message;
    private String remediation;
    private LocalDateTime createdAt;
}
