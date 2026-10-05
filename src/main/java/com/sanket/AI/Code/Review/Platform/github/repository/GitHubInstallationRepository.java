package com.sanket.AI.Code.Review.Platform.github.repository;

import com.sanket.AI.Code.Review.Platform.github.model.GitHubInstallation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GitHubInstallationRepository extends JpaRepository<GitHubInstallation, Long> {

    Optional<GitHubInstallation> findByInstallationId(Long installationId);

    List<GitHubInstallation> findByUserIdAndStatus(Long userId, String status);

    List<GitHubInstallation> findByUserId(Long userId);

    boolean existsByInstallationId(Long installationId);
}
