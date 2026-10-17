package com.sanket.AI.Code.Review.Platform.report.repository;

import com.sanket.AI.Code.Review.Platform.report.entity.FileWiseFinding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileWiseFindingRepository extends JpaRepository<FileWiseFinding, Long> {

    List<FileWiseFinding> findByReportId(Long reportId);
}
