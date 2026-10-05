package com.sanket.AI.Code.Review.Platform.review.repository;

import com.sanket.AI.Code.Review.Platform.review.entity.FindingSeverity;
import com.sanket.AI.Code.Review.Platform.review.entity.ReviewFinding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewFindingRepository extends JpaRepository<ReviewFinding, Long> {

    List<ReviewFinding> findByReviewJobIdOrderBySeverityAsc(Long reviewJobId);

    List<ReviewFinding> findByReviewJobJobReference(String jobReference);

    long countByReviewJobProjectIdAndSeverity(Long projectId, FindingSeverity severity);
}
