package com.sanket.AI.Code.Review.Platform.report.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "report_review_insights", indexes = {
        @Index(name = "idx_rri_report_id", columnList = "report_id"),
        @Index(name = "idx_rri_category", columnList = "category")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewInsight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @Column(nullable = false)
    private String category; // SECURITY, ARCHITECTURE, PERFORMANCE, CODE_QUALITY, TEST_COVERAGE

    @Column(nullable = false)
    private String impactArea; // HIGH, MEDIUM, LOW

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String actionRequired;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
