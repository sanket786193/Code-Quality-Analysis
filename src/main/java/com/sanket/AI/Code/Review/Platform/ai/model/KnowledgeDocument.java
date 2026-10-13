package com.sanket.AI.Code.Review.Platform.ai.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_knowledge_documents", indexes = {
        @Index(name = "idx_kd_project_id", columnList = "project_id"),
        @Index(name = "idx_kd_category", columnList = "category"),
        @Index(name = "idx_kd_language", columnList = "language")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id")
    private Long projectId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String category; // CODING_STANDARD, SECURITY_RULE, ARCHITECTURE, PAST_REVIEW

    private String language; // JAVA, TYPESCRIPT, PYTHON, ALL

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private String ruleKey;

    @Column(name = "embedding", columnDefinition = "TEXT")
    private String embeddingJson; // serialized vector float array for compatibility & pgvector casting

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.language == null) {
            this.language = "JAVA";
        }
    }
}
