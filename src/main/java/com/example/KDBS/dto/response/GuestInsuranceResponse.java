package com.example.KDBS.dto.response;

import com.example.KDBS.enums.InsuranceStatus;
import lombok.Data;

@Data
public class GuestInsuranceResponse {
    private BookingGuestResponse guestResponse;
    private String insuranceNumber;
    private InsuranceStatus insuranceStatus;
}
