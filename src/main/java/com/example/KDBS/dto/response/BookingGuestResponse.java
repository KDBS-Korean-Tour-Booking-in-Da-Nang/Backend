package com.example.KDBS.dto.response;

import com.example.KDBS.enums.Gender;
import com.example.KDBS.enums.BookingGuestType;
import com.example.KDBS.enums.InsuranceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingGuestResponse {
    private Long bookingGuestId;
    private String fullName;
    private LocalDate birthDate;
    private Gender gender;
    private String idNumber;
    private String nationality;
    private BookingGuestType bookingGuestType;
    private InsuranceStatus insuranceStatus;
}