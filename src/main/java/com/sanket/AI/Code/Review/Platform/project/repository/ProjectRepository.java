package com.sanket.AI.Code.Review.Platform.project.repository;

import com.sanket.AI.Code.Review.Platform.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    boolean existsByNameAndOwnerId(String name, Long ownerId);

    Page<Project> findByOwnerId(Long ownerId, Pageable pageable);

    Page<Project> findByTeamLeadId(Long teamLeadId, Pageable pageable);

    Page<Project> findByOwnerIdOrTeamLeadId(Long ownerId, Long teamLeadId, Pageable pageable);

    Optional<Project> findByIdAndOwnerId(Long id, Long ownerId);

    @EntityGraph(attributePaths = {"owner", "teamLead", "repositoryMapping"})
    Optional<Project> findWithDetailsById(Long id);
}
