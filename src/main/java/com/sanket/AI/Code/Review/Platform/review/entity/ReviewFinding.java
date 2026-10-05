package com.sanket.AI.Code.Review.Platform.review.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "review_findings", indexes = {
        @Index(name = "idx_finding_job_id", columnList = "review_job_id"),
        @Index(name = "idx_finding_severity", columnList = "severity")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewFinding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_job_id", nullable = false)
    private ReviewJob reviewJob;

    @Column(nullable = false)
    private String filePath;

    private Integer lineNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FindingSeverity severity;

    @Column(nullable = false)
    private String category; // SECURITY, CODE_SMELL, PERFORMANCE, BUG, GOVERNANCE

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String suggestedFix;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
