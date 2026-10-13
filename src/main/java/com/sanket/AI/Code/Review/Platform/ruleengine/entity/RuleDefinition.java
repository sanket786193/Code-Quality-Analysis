package com.sanket.AI.Code.Review.Platform.ruleengine.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "rule_definitions", indexes = {
        @Index(name = "idx_rule_key", columnList = "ruleKey"),
        @Index(name = "idx_rule_category", columnList = "category"),
        @Index(name = "idx_rule_enabled", columnList = "enabled")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String ruleKey;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private RuleCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RuleSeverity severity;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String regexPattern;

    @Column(columnDefinition = "TEXT")
    private String negativePattern;

    @Column(columnDefinition = "TEXT")
    private String remediation;

    @Builder.Default
    @Column(nullable = false)
    private Boolean enabled = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.enabled == null) this.enabled = true;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
