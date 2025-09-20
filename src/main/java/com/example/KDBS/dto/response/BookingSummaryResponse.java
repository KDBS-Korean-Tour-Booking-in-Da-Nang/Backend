package com.example.KDBS.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingSummaryResponse {

    private Long bookingId;
    private Long tourId;
    private String tourName;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private LocalDate departureDate;
    private Integer totalGuests;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
}
