package com.sanket.AI.Code.Review.Platform.report.entity;

import com.sanket.AI.Code.Review.Platform.project.entity.Project;
import com.sanket.AI.Code.Review.Platform.review.entity.ReviewJob;
import com.sanket.AI.Code.Review.Platform.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "reports", indexes = {
        @Index(name = "idx_report_ref", columnList = "reportReference"),
        @Index(name = "idx_report_job_id", columnList = "review_job_id"),
        @Index(name = "idx_report_project_id", columnList = "project_id"),
        @Index(name = "idx_report_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String reportReference;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_job_id", nullable = false)
    private ReviewJob reviewJob;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private String pullRequestTitle;

    @Column(nullable = false)
    private String authorUsername;

    @Column(nullable = false)
    private String branchName;

    private String targetBranch;

    @Column(nullable = false)
    private String commitSha;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.GENERATED;

    @Builder.Default
    @Column(nullable = false)
    private Double overallScore = 100.0;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private ReviewRecommendation recommendation = ReviewRecommendation.APPROVE;

    @Column(columnDefinition = "TEXT")
    private String decisionComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decision_by_user_id")
    private User decisionBy;

    private LocalDateTime generatedAt;

    private LocalDateTime decidedAt;

    @OneToOne(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ReportSummary summary;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ReviewInsight> insights = new ArrayList<>();

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<FileWiseFinding> fileFindings = new ArrayList<>();

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ComparisonSection> comparisons = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.reportReference == null || this.reportReference.isBlank()) {
            this.reportReference = "RPT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        if (this.generatedAt == null) {
            this.generatedAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = ReportStatus.GENERATED;
        }
        if (this.recommendation == null) {
            this.recommendation = ReviewRecommendation.APPROVE_WITH_CHANGES;
        }
    }
}
