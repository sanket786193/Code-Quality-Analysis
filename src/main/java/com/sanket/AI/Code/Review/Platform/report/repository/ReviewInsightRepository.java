package com.sanket.AI.Code.Review.Platform.report.repository;

import com.sanket.AI.Code.Review.Platform.report.entity.ReviewInsight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewInsightRepository extends JpaRepository<ReviewInsight, Long> {

    List<ReviewInsight> findByReportId(Long reportId);
}
