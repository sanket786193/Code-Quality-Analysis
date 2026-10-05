package com.sanket.AI.Code.Review.Platform.project.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.sanket.AI.Code.Review.Platform.github.model.GitHubInstallation;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "repository_mappings", indexes = {
        @Index(name = "idx_repo_mapping_github_id", columnList = "githubRepoId"),
        @Index(name = "idx_repo_mapping_full_name", columnList = "repoFullName")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepositoryMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false, unique = true)
    @JsonBackReference
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "installation_id", nullable = false)
    private GitHubInstallation installation;

    @Column(nullable = false)
    private Long githubRepoId;

    @Column(nullable = false)
    private String repoName;

    @Column(nullable = false)
    private String repoFullName;

    @Column(nullable = false)
    private String repoUrl;

    @Builder.Default
    private String defaultBranch = "main";

    @Builder.Default
    @Column(nullable = false)
    private Boolean isPrivate = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean webhookActive = true;

    private LocalDateTime connectedAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.connectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
