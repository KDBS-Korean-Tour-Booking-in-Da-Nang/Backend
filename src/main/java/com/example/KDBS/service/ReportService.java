package com.example.KDBS.service;

import com.example.KDBS.dto.request.ReportRequest;
import com.example.KDBS.dto.request.UpdateReportRequest;
import com.example.KDBS.dto.response.ReportResponse;
import com.example.KDBS.dto.response.ReportSummaryResponse;
import com.example.KDBS.enums.ReportStatus;
import com.example.KDBS.enums.ReportTargetType;
import com.example.KDBS.enums.StaffTask;
import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.example.KDBS.mapper.ReportMapper;
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

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ForumPostRepository forumPostRepository;
    private final ForumCommentRepository forumCommentRepository;
    private final ReportMapper reportMapper;
    private final StaffService staffService;

    @Transactional
    public ReportResponse createReport(ReportRequest request, String userEmail) {
        // tim user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        // check target exits
        validateTarget(request.getTargetType(), request.getTargetId());

        // check user da report target nay chua
        if (reportRepository.existsByReporterAndTargetTypeAndTargetId((long) user.getUserId(), request.getTargetType(),
                request.getTargetId())) {
            throw new AppException(ErrorCode.ALREADY_REPORTED);
        }

        // tao report
        Report report = reportMapper.toEntity(request);
        report.setReporter(user);

        Report savedReport = reportRepository.save(report);
        ReportResponse response = reportMapper.toResponse(savedReport);

        response.setTargetTitle(getTargetTitle(savedReport.getTargetType(), savedReport.getTargetId()));
        response.setTargetAuthor(getTargetAuthor(savedReport.getTargetType(), savedReport.getTargetId()));

        return response;
    }

    @Transactional
    public ReportResponse updateReportStatus(Long reportId, UpdateReportRequest request, String adminEmail) {
        staffService.getAuthorizedStaff(StaffTask.FORUM_REPORT_AND_BOOKING_COMPLAINT);

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
        return reportMapper.toResponse(updatedReport);
    }

    @Transactional(readOnly = true)
    public Page<ReportSummaryResponse> getAllReports(Pageable pageable) {
        Page<Report> reports = reportRepository.findAllByOrderByReportedAtDesc(pageable);
        return reports.map(reportMapper::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    public ReportResponse getReportById(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new AppException(ErrorCode.REPORT_NOT_FOUND));

        return reportMapper.toResponse(report);
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

    @Transactional(readOnly = true)
    public boolean hasUserReported(String userEmail, ReportTargetType targetType, Long targetId) {
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            return false;
        }
        return reportRepository.existsByReporterAndTargetTypeAndTargetId((long) user.getUserId(), targetType, targetId);
    }

    private void validateTarget(ReportTargetType targetType, Long targetId) {
        switch (targetType) {
            case POST -> {
                if (!forumPostRepository.existsById(targetId)) {
                    throw new AppException(ErrorCode.POST_NOT_FOUND);
                }
            }
            case COMMENT -> {
                if (!forumCommentRepository.existsById(targetId)) {
                    throw new AppException(ErrorCode.COMMENT_NOT_FOUND);
                }
            }
        }
    }

    private String getTargetTitle(ReportTargetType targetType, Long targetId) {
        try {
            return switch (targetType) {
                case POST -> {
                    ForumPost post = forumPostRepository.findById(targetId).orElse(null);
                    yield post != null ? post.getTitle() : "Post not found";
                }
                case COMMENT -> {
                    ForumComment comment = forumCommentRepository.findById(targetId).orElse(null);
                    yield comment != null
                            ? comment.getContent().substring(0, Math.min(comment.getContent().length(), 100)) + "..."
                            : "Comment not found";
                }
            };
        } catch (Exception e) {
            return "Error loading target";
        }
    }

    private String getTargetAuthor(ReportTargetType targetType, Long targetId) {
        try {
            return switch (targetType) {
                case POST -> {
                    ForumPost post = forumPostRepository.findById(targetId).orElse(null);
                    yield post != null ? post.getUser().getUsername() : "Unknown";
                }
                case COMMENT -> {
                    ForumComment comment = forumCommentRepository.findById(targetId).orElse(null);
                    yield comment != null ? comment.getUser().getUsername() : "Unknown";
                }
            };
        } catch (Exception e) {
            return "Error loading author";
        }
    }
}
