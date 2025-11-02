package com.example.KDBS.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class InsuranceRequest {
    @NotNull(message = "Tour ID is required")
    private Long tourId;
    @NotNull(message = "Booking ID is required")
    private Long bookingId;
    private List<Long> bookingGuessIds;
}
