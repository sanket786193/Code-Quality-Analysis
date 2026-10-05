package com.sanket.AI.Code.Review.Platform.project.repository;

import com.sanket.AI.Code.Review.Platform.project.entity.RepositoryMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepositoryMappingRepository extends JpaRepository<RepositoryMapping, Long> {

    Optional<RepositoryMapping> findByGithubRepoId(Long githubRepoId);

    Optional<RepositoryMapping> findByRepoFullName(String repoFullName);

    Optional<RepositoryMapping> findByProjectId(Long projectId);

    boolean existsByGithubRepoId(Long githubRepoId);

    boolean existsByProjectId(Long projectId);
}
