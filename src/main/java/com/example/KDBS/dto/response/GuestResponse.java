package com.example.KDBS.dto.response;

import com.example.KDBS.enums.Gender;
import com.example.KDBS.enums.GuestType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuestResponse {
    private Long guestId;
    private String fullName;
    private LocalDate birthDate;
    private Gender gender;
    private String idNumber;
    private String nationality;
    private GuestType guestType;
}