package com.sanket.AI.Code.Review.Platform.review.repository;

import com.sanket.AI.Code.Review.Platform.review.entity.ReviewSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewSummaryRepository extends JpaRepository<ReviewSummary, Long> {

    Optional<ReviewSummary> findByReviewJobId(Long reviewJobId);

    Optional<ReviewSummary> findByReviewJobJobReference(String jobReference);
}
