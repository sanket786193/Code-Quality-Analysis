package com.sanket.AI.Code.Review.Platform.report.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "report_summaries", indexes = {
        @Index(name = "idx_rep_summary_report_id", columnList = "report_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false, unique = true)
    private Report report;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String executiveSummary;

    @Builder.Default
    @Column(nullable = false)
    private Integer totalFiles = 0;

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
    private String qualityGateStatus = "PASSED";

    @Column(columnDefinition = "TEXT")
    private String keyRisks;

    @Column(columnDefinition = "TEXT")
    private String architectureImpact;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
