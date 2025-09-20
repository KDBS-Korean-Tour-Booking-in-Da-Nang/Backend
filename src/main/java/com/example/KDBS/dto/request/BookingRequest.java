package com.example.KDBS.dto.request;

import com.example.KDBS.enums.Gender;
import com.example.KDBS.enums.GuestType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequest {

    @NotNull(message = "Tour ID is required")
    private Long tourId;

    @NotBlank(message = "Contact name is required")
    @Size(max = 100, message = "Contact name must not exceed 100 characters")
    private String contactName;

    @Size(max = 255, message = "Contact address must not exceed 255 characters")
    private String contactAddress;

    @NotBlank(message = "Contact phone is required")
    @Pattern(regexp = "^[0-9+\\-\\s()]+$", message = "Invalid phone number format")
    @Size(max = 20, message = "Contact phone must not exceed 20 characters")
    private String contactPhone;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Contact email must not exceed 100 characters")
    private String contactEmail;

    @Size(max = 255, message = "Pickup point must not exceed 255 characters")
    private String pickupPoint;

    private String note;

    @NotNull(message = "Departure date is required")
    @Future(message = "Departure date must be in the future")
    private LocalDate departureDate;

    @Min(value = 0, message = "Adults count must be non-negative")
    private Integer adultsCount = 0;

    @Min(value = 0, message = "Children count must be non-negative")
    private Integer childrenCount = 0;

    @Min(value = 0, message = "Babies count must be non-negative")
    private Integer babiesCount = 0;

    @Valid
    @NotEmpty(message = "At least one guest is required")
    private List<GuestRequest> guests;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GuestRequest {
        @NotBlank(message = "Guest full name is required")
        @Size(max = 100, message = "Guest full name must not exceed 100 characters")
        private String fullName;

        @NotNull(message = "Birth date is required")
        private LocalDate birthDate;

        private Gender gender;

        @Size(max = 50, message = "ID number must not exceed 50 characters")
        private String idNumber;

        @Size(max = 50, message = "Nationality must not exceed 50 characters")
        private String nationality;

        @NotNull(message = "Guest type is required")
        private GuestType guestType;
    }
}
