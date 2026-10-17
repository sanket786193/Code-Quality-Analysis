package com.sanket.AI.Code.Review.Platform.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RagQueryRequest {

    private Long projectId;

    @NotBlank(message = "Query text is required")
    private String query;

    private String category;

    private String language;

    @Builder.Default
    private Integer topK = 5;

    @Builder.Default
    private Double minSimilarity = 0.65;
}
