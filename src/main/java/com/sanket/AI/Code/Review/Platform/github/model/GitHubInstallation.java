package com.sanket.AI.Code.Review.Platform.github.model;

import com.sanket.AI.Code.Review.Platform.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "github_installations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GitHubInstallation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long installationId;

    @Column(nullable = false)
    private String accountLogin;

    @Column(nullable = false)
    private String accountType; // User or Organization

    private String repositorySelection; // ALL or SELECTED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder.Default
    @Column(nullable = false)
    private String status = "ACTIVE"; // ACTIVE, SUSPENDED, DELETED

    private String permissions;

    private LocalDateTime installedAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.installedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "ACTIVE";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
