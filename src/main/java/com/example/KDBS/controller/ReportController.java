package com.example.KDBS.controller;

import com.example.KDBS.dto.request.ReportRequest;
import com.example.KDBS.dto.request.UpdateReportRequest;
import com.example.KDBS.dto.response.ReportResponse;
import com.example.KDBS.dto.response.ReportSummaryResponse;
import com.example.KDBS.enums.ReportStatus;
import com.example.KDBS.enums.ReportTargetType;
import com.example.KDBS.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/create")
    public ResponseEntity<ReportResponse> createReport(
            @RequestBody ReportRequest request,
            @RequestParam String userEmail) {
        ReportResponse response = reportService.createReport(request, userEmail);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{reportId}/status")
    public ResponseEntity<ReportResponse> updateReportStatus(
            @PathVariable Long reportId,
            @RequestBody UpdateReportRequest request,
            @RequestParam String adminEmail) {
        ReportResponse response = reportService.updateReportStatus(reportId, request, adminEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/all")
    public ResponseEntity<Page<ReportSummaryResponse>> getAllReports(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ReportSummaryResponse> reports = reportService.getAllReports(pageable);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/admin/stats")
    public ResponseEntity<Object> getReportStats() {
        Map<ReportStatus, Long> stats = reportService.getReportStatsByStatus();

        return ResponseEntity.ok(new Object() {
            public final Long pending = stats.get(ReportStatus.PENDING);
            public final Long investigating = stats.get(ReportStatus.INVESTIGATING);
            public final Long resolved = stats.get(ReportStatus.RESOLVED);
            public final Long dismissed = stats.get(ReportStatus.DISMISSED);
            public final Long closed = stats.get(ReportStatus.CLOSED);
            public final Long total = stats.values().stream().mapToLong(Long::longValue).sum();
        });
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkUserReported(
            @RequestParam String userEmail,
            @RequestParam String targetType,
            @RequestParam Long targetId) {
        boolean hasReported = reportService.hasUserReported(userEmail, ReportTargetType.valueOf(targetType), targetId);
        return ResponseEntity.ok(hasReported);
    }
}
