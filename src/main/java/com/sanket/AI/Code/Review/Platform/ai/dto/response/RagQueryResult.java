package com.sanket.AI.Code.Review.Platform.ai.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RagQueryResult {

    private Long documentId;
    private String title;
    private String category;
    private String language;
    private String content;
    private String ruleKey;
    private Double similarityScore;
}
