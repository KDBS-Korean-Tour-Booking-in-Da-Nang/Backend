package com.example.KDBS.dto.response;

import com.example.KDBS.enums.ComplaintResolutionType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingComplaintResponse {
    private Long complaintId;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private ComplaintResolutionType resolutionType;
}


