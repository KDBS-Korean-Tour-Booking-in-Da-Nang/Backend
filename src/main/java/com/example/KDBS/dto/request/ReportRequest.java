package com.example.KDBS.dto.request;

import com.example.KDBS.enums.ReportTargetType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {

    @NotNull(message = "Target type is required")
    private ReportTargetType targetType;

    @NotNull(message = "Target ID is required")
    private Long targetId;

    @NotEmpty(message = "At least one reason is required")
    private Set<String> reasons;

    private String description;
}
