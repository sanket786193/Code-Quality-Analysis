package com.sanket.AI.Code.Review.Platform.ruleengine.dto.request;

import com.sanket.AI.Code.Review.Platform.ruleengine.entity.RuleCategory;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluateCodeRequest {

    @NotBlank(message = "Code snippet cannot be blank")
    private String codeSnippet;

    @Builder.Default
    private String fileName = "Snippet.java";

    private RuleCategory category;
}
