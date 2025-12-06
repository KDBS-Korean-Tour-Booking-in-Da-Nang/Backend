package com.example.KDBS.dto.request;

import com.example.KDBS.enums.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReportRequest {

    @NotNull(message = "Status is required")
    private ReportStatus status;
    private String adminNote;
    private String handledBy;
}
