package com.example.KDBS.dto.response;

import com.example.KDBS.enums.TicketReasonType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {
    private int userId;
    private String message;
    private List<TicketReasonType> reasons;
    private LocalDateTime createdAt;
}
