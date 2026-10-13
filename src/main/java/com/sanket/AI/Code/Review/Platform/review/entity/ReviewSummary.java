package com.sanket.AI.Code.Review.Platform.review.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "review_summaries", indexes = {
        @Index(name = "idx_review_summary_job_id", columnList = "review_job_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_job_id", nullable = false, unique = true)
    private ReviewJob reviewJob;

    @Builder.Default
    @Column(nullable = false)
    private Integer totalFilesAnalyzed = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer criticalCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer highCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer mediumCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer lowCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private String sonarQualityGateStatus = "PASSED";

    @Column(columnDefinition = "TEXT")
    private String executiveSummary;

    @Column(columnDefinition = "TEXT")
    private String actionableAdvice;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
