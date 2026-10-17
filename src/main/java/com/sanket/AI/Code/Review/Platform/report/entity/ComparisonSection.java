package com.sanket.AI.Code.Review.Platform.report.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "report_comparisons", indexes = {
        @Index(name = "idx_rc_report_id", columnList = "report_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComparisonSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @Column(nullable = false)
    private String fileName;

    private Integer lineNumber;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String beforeChange;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String afterChange;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String aiSuggestion;

    @Column(columnDefinition = "TEXT")
    private String actualImpact;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
