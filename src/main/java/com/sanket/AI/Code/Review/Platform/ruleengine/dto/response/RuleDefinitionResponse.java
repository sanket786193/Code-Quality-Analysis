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
public class RuleDefinitionResponse {

    private Long id;
    private String ruleKey;
    private String name;
    private RuleCategory category;
    private String categoryDisplayName;
    private RuleSeverity severity;
    private String description;
    private String regexPattern;
    private String negativePattern;
    private String remediation;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
