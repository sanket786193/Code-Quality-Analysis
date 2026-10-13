package com.sanket.AI.Code.Review.Platform.report.repository;

import com.sanket.AI.Code.Review.Platform.report.entity.ReportSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReportSummaryRepository extends JpaRepository<ReportSummary, Long> {

    Optional<ReportSummary> findByReportId(Long reportId);
}
