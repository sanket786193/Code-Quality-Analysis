package com.sanket.AI.Code.Review.Platform.review.dto.response;

import com.sanket.AI.Code.Review.Platform.review.entity.FindingSeverity;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewFindingResponse {
    private Long id;
    private String filePath;
    private Integer lineNumber;
    private FindingSeverity severity;
    private String category;
    private String title;
    private String description;
    private String suggestedFix;
    private LocalDateTime createdAt;
}
