package com.example.KDBS.dto.response;

import com.example.KDBS.enums.ReportStatus;
import com.example.KDBS.enums.ReportTargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportSummaryResponse {

    private Long reportId;
    private ReportTargetType targetType;
    private Long targetId;
    private String targetTitle;
    private String targetAuthor;
    private String reporterUsername;
    private String reasons; // Join các reasons thành string
    private ReportStatus status;
    private LocalDateTime reportedAt;
    private Long reportCount; // Số lượng report cho target này
}
