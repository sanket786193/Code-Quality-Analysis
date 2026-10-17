package com.sanket.AI.Code.Review.Platform.report.repository;

import com.sanket.AI.Code.Review.Platform.report.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    Optional<Report> findByReportReference(String reportReference);

    Optional<Report> findByReviewJobJobReference(String jobReference);

    Optional<Report> findByReviewJobId(Long reviewJobId);

    Optional<Report> findFirstByReviewJobPullRequestNumberOrderByGeneratedAtDesc(Integer pullRequestNumber);

    Page<Report> findByProjectId(Long projectId, Pageable pageable);

    List<Report> findTop5ByProjectIdOrderByGeneratedAtDesc(Long projectId);

    Page<Report> findByAuthorUsername(String authorUsername, Pageable pageable);

    @Query("SELECT COUNT(r) FROM Report r WHERE r.project.id = :projectId AND r.status = com.sanket.AI.Code.Review.Platform.report.entity.ReportStatus.APPROVED")
    long countApprovedByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT COUNT(r) FROM Report r WHERE r.project.id = :projectId AND r.status = com.sanket.AI.Code.Review.Platform.report.entity.ReportStatus.CHANGES_REQUESTED")
    long countChangesRequestedByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT COUNT(r) FROM Report r WHERE r.project.id = :projectId AND r.status = com.sanket.AI.Code.Review.Platform.report.entity.ReportStatus.REJECTED")
    long countRejectedByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT AVG(r.overallScore) FROM Report r WHERE r.project.id = :projectId")
    Double getAverageScoreByProjectId(@Param("projectId") Long projectId);
}
