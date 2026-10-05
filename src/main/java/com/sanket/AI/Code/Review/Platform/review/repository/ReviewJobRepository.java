package com.sanket.AI.Code.Review.Platform.review.repository;

import com.sanket.AI.Code.Review.Platform.review.entity.ReviewJob;
import com.sanket.AI.Code.Review.Platform.review.entity.ReviewJobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewJobRepository extends JpaRepository<ReviewJob, Long> {

    Optional<ReviewJob> findByJobReference(String jobReference);

    Page<ReviewJob> findByProjectIdOrderByQueuedAtDesc(Long projectId, Pageable pageable);

    List<ReviewJob> findTop5ByProjectIdOrderByQueuedAtDesc(Long projectId);

    long countByProjectId(Long projectId);

    long countByProjectIdAndStatus(Long projectId, ReviewJobStatus status);
}
