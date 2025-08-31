package com.example.KDBS.repository;

import com.example.KDBS.enums.ReportStatus;
import com.example.KDBS.enums.ReportTargetType;
import com.example.KDBS.model.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    // get all report with phan trang
    Page<Report> findAllByOrderByReportedAtDesc(Pageable pageable);
    // thong ke report
    @Query(value = "SELECT status, COUNT(*) as count FROM reports GROUP BY status", nativeQuery = true)
    List<Object[]> getReportStatsByStatus();
}
