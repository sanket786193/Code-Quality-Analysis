package com.sanket.AI.Code.Review.Platform.report.entity;

import com.sanket.AI.Code.Review.Platform.review.entity.FindingSeverity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "report_file_findings", indexes = {
        @Index(name = "idx_rff_report_id", columnList = "report_id"),
        @Index(name = "idx_rff_severity", columnList = "severity")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileWiseFinding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @Column(nullable = false)
    private String fileName;

    private Integer lineNumber;

    @Column(nullable = false)
    private String issueType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FindingSeverity severity;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String explanation;

    @Column(columnDefinition = "TEXT")
    private String suggestedFix;

    private Double confidenceScore;

    private String ruleKey;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
