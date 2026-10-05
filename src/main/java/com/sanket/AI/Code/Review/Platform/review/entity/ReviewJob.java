package com.sanket.AI.Code.Review.Platform.review.entity;

import com.sanket.AI.Code.Review.Platform.project.entity.Project;
import com.sanket.AI.Code.Review.Platform.project.entity.RepositoryMapping;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "review_jobs", indexes = {
        @Index(name = "idx_review_job_reference", columnList = "jobReference"),
        @Index(name = "idx_review_job_project_id", columnList = "project_id"),
        @Index(name = "idx_review_job_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String jobReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_mapping_id", nullable = false)
    private RepositoryMapping repositoryMapping;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewEventType eventType;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private ReviewJobStatus status = ReviewJobStatus.QUEUED;

    @Column(nullable = false)
    private String commitSha;

    private Integer pullRequestNumber;

    private String pullRequestTitle;

    @Column(nullable = false)
    private String branchName;

    @Column(nullable = false)
    private String senderUsername;

    @Builder.Default
    @Column(nullable = false)
    private Integer findingsCount = 0;

    @Column(columnDefinition = "TEXT")
    private String summary;

    private LocalDateTime queuedAt;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "reviewJob", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ReviewFinding> findings = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.jobReference == null || this.jobReference.isBlank()) {
            this.jobReference = "REV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        if (this.queuedAt == null) {
            this.queuedAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = ReviewJobStatus.QUEUED;
        }
        if (this.findingsCount == null) {
            this.findingsCount = 0;
        }
    }
}
