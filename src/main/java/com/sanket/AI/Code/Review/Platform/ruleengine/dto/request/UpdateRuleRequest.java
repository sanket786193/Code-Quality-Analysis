package com.sanket.AI.Code.Review.Platform.ruleengine.dto.request;

import com.sanket.AI.Code.Review.Platform.ruleengine.entity.RuleCategory;
import com.sanket.AI.Code.Review.Platform.ruleengine.entity.RuleSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateRuleRequest {

    @NotBlank(message = "Rule name is required")
    private String name;

    @NotNull(message = "Category is required")
    private RuleCategory category;

    @NotNull(message = "Severity is required")
    private RuleSeverity severity;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Regex pattern is required")
    private String regexPattern;

    private String negativePattern;

    private String remediation;

    private Boolean enabled;
}
