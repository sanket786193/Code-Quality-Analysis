package com.sanket.AI.Code.Review.Platform.report.repository;

import com.sanket.AI.Code.Review.Platform.report.entity.ComparisonSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComparisonSectionRepository extends JpaRepository<ComparisonSection, Long> {

    List<ComparisonSection> findByReportId(Long reportId);
}
