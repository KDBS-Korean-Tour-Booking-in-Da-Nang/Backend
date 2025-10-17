package com.example.KDBS.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class InsuranceResponse {
    private Long tourId;
    private Long bookingId;
    private List<GuestInsuranceResponse> guestInsuranceResponses;
}
