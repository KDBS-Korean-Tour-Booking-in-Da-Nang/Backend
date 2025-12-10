package com.example.KDBS.dto.response;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BookingSimpleResponse {
    private Long bookingId;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private LocalDate departureDate;
    private String status;
    private LocalDateTime createdAt;
}
