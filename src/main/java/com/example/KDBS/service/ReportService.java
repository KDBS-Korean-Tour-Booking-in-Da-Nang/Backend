package com.example.KDBS.service;

import com.example.KDBS.dto.request.ReportRequest;
import com.example.KDBS.dto.request.UpdateReportRequest;
import com.example.KDBS.dto.response.ReportResponse;
import com.example.KDBS.dto.response.ReportSummaryResponse;
import com.example.KDBS.enums.ReportStatus;
import com.example.KDBS.enums.ReportTargetType;
import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.example.KDBS.model.ForumComment;
import com.example.KDBS.model.ForumPost;
import com.example.KDBS.model.Report;
import com.example.KDBS.model.User;
import com.example.KDBS.repository.ForumCommentRepository;
import com.example.KDBS.repository.ForumPostRepository;
import com.example.KDBS.repository.ReportRepository;
import com.example.KDBS.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ForumPostRepository forumPostRepository;
    private final ForumCommentRepository forumCommentRepository;

    @Transactional
    public ReportResponse createReport(ReportRequest request, String userEmail) {
        // tim user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        // check target exits
        validateTarget(request.getTargetType(), request.getTargetId());
        // tao report
        Report report = Report.builder()
                .reporter(user)
                .targetType(request.getTargetType())
                .targetId(request.getTargetId())
                .reasons(request.getReasons())
                .description(request.getDescription())
                .build();

        Report savedReport = reportRepository.save(report);
        return mapToResponse(savedReport);
    }


    @Transactional
    public ReportResponse updateReportStatus(Long reportId, UpdateReportRequest request, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new AppException(ErrorCode.REPORT_NOT_FOUND));

        report.setStatus(request.getStatus());
        report.setAdminNote(request.getAdminNote());

        if (request.getStatus() == ReportStatus.RESOLVED || request.getStatus() == ReportStatus.DISMISSED) {
            report.setResolvedAt(LocalDateTime.now());
            report.setResolvedBy(admin);
        }

        Report updatedReport = reportRepository.save(report);
        return mapToResponse(updatedReport);
    }


    @Transactional(readOnly = true)
    public Page<ReportSummaryResponse> getAllReports(Pageable pageable) {
        Page<Report> reports = reportRepository.findAllByOrderByReportedAtDesc(pageable);
        return reports.map(this::mapToSummaryResponse);
    }
    

    @Transactional(readOnly = true)
    public Map<ReportStatus, Long> getReportStatsByStatus() {
        List<Object[]> stats = reportRepository.getReportStatsByStatus();
        Map<ReportStatus, Long> result = new HashMap<>();

        for (ReportStatus status : ReportStatus.values()) {
            result.put(status, 0L);
        }
        
        for (Object[] stat : stats) {
            String statusStr = (String) stat[0];
            Long count = ((Number) stat[1]).longValue();
            try {
                ReportStatus status = ReportStatus.valueOf(statusStr);
                result.put(status, count);
            } catch (IllegalArgumentException e) {
                // Bỏ qua status không hợp lệ
            }
        }
        
        return result;
    }


    private void validateTarget(ReportTargetType targetType, Long targetId) {
        switch (targetType) {
            case POST:
                if (!forumPostRepository.existsById(targetId)) {
                    throw new AppException(ErrorCode.POST_NOT_FOUND);
                }
                break;
            case COMMENT:
                if (!forumCommentRepository.existsById(targetId)) {
                    throw new AppException(ErrorCode.COMMENT_NOT_FOUND);
                }
                break;
        }
    }


    private ReportResponse mapToResponse(Report report) {
        String targetTitle = getTargetTitle(report.getTargetType(), report.getTargetId());
        String targetAuthor = getTargetAuthor(report.getTargetType(), report.getTargetId());

        return ReportResponse.builder()
                .reportId(report.getReportId())
                .reporterUsername(report.getReporter().getUsername())
                .reporterEmail(report.getReporter().getEmail())
                .targetType(report.getTargetType())
                .targetId(report.getTargetId())
                .targetTitle(targetTitle)
                .targetAuthor(targetAuthor)
                .reasons(report.getReasons())
                .description(report.getDescription())
                .status(report.getStatus())
                .adminNote(report.getAdminNote())
                .reportedAt(report.getReportedAt())
                .resolvedAt(report.getResolvedAt())
                .resolvedByUsername(report.getResolvedBy() != null ? report.getResolvedBy().getUsername() : null)
                .build();
    }


    private ReportSummaryResponse mapToSummaryResponse(Report report) {
        String targetTitle = getTargetTitle(report.getTargetType(), report.getTargetId());
        String targetAuthor = getTargetAuthor(report.getTargetType(), report.getTargetId());

        return ReportSummaryResponse.builder()
                .reportId(report.getReportId())
                .targetType(report.getTargetType())
                .targetId(report.getTargetId())
                .targetTitle(targetTitle)
                .targetAuthor(targetAuthor)
                .reporterUsername(report.getReporter().getUsername())
                .reasons(String.join(", ", report.getReasons()))
                .status(report.getStatus())
                .reportedAt(report.getReportedAt())
                .reportCount(0L)
                .build();
    }


    private String getTargetTitle(ReportTargetType targetType, Long targetId) {
        try {
            switch (targetType) {
                case POST:
                    ForumPost post = forumPostRepository.findById(targetId).orElse(null);
                    return post != null ? post.getTitle() : "Post not found";
                case COMMENT:
                    ForumComment comment = forumCommentRepository.findById(targetId).orElse(null);
                    return comment != null ? comment.getContent().substring(0, Math.min(comment.getContent().length(), 100)) + "..." : "Comment not found";
                default:
                    return "Unknown target";
            }
        } catch (Exception e) {
            return "Error loading target";
        }
    }


    private String getTargetAuthor(ReportTargetType targetType, Long targetId) {
        try {
            switch (targetType) {
                case POST:
                    ForumPost post = forumPostRepository.findById(targetId).orElse(null);
                    return post != null ? post.getUser().getUsername() : "Unknown";
                case COMMENT:
                    ForumComment comment = forumCommentRepository.findById(targetId).orElse(null);
                    return comment != null ? comment.getUser().getUsername() : "Unknown";
                default:
                    return "Unknown";
            }
        } catch (Exception e) {
            return "Error loading author";
        }
    }
}
