package com.example.KDBS.dto.response;

import com.example.KDBS.enums.ReportStatus;
import com.example.KDBS.enums.ReportTargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {

    private Long reportId;
    private String reporterUsername;
    private String reporterEmail;
    private ReportTargetType targetType;
    private Long targetId;
    private String targetTitle; // post title/ comment content
    private String targetAuthor; // author cua post/comment
    private Set<String> reasons;
    private String description;
    private ReportStatus status;
    private String adminNote;
    private LocalDateTime reportedAt;
    private LocalDateTime resolvedAt;
    private String resolvedByUsername;
}
