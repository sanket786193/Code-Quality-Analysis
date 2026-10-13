package com.sanket.AI.Code.Review.Platform.review.rabbitmq.message;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewJobMessage implements Serializable {

    private Long reviewJobId;
    private String jobReference;
    private Long projectId;
    private String commitSha;
    private String branchName;
    private Integer pullRequestNumber;
    private String eventType;
}
