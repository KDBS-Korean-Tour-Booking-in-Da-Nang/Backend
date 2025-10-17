package com.example.KDBS.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class InsuranceRequest {
    private Long tourId;
    private Long bookingId;
    private List<Long> bookingGuessIds;
}
