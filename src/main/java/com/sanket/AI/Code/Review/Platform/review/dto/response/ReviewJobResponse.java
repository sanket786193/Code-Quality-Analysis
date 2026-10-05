package com.sanket.AI.Code.Review.Platform.review.dto.response;

import com.sanket.AI.Code.Review.Platform.review.entity.ReviewEventType;
import com.sanket.AI.Code.Review.Platform.review.entity.ReviewJobStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewJobResponse {
    private Long id;
    private String jobReference;
    private Long projectId;
    private String projectName;
    private String repoFullName;
    private ReviewEventType eventType;
    private ReviewJobStatus status;
    private String commitSha;
    private Integer pullRequestNumber;
    private String pullRequestTitle;
    private String branchName;
    private String senderUsername;
    private Integer findingsCount;
    private String summary;
    private LocalDateTime queuedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
