package com.example.KDBS.dto.response;

import com.example.KDBS.enums.Gender;
import com.example.KDBS.enums.GuestType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {

    private Long bookingId;
    private Long tourId;
    private String tourName;
    private String contactName;
    private String contactAddress;
    private String contactPhone;
    private String contactEmail;
    private String pickupPoint;
    private String note;
    private LocalDate departureDate;
    private Integer adultsCount;
    private Integer childrenCount;
    private Integer babiesCount;
    private Integer totalGuests;
    private LocalDateTime createdAt;
    private List<GuestResponse> guests;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GuestResponse {
        private Long guestId;
        private String fullName;
        private LocalDate birthDate;
        private Gender gender;
        private String idNumber;
        private String nationality;
        private GuestType guestType;
    }
}
