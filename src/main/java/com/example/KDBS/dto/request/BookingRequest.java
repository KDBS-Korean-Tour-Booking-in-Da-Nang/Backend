package com.example.KDBS.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingRequest {

    @NotNull(message = "Tour ID is required")
    private Long tourId;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email email must not exceed 100 characters")
    @NotBlank(message = "User email is required")
    private String userEmail;

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
    @NotBlank(message = "Contact email is required")
    private String contactEmail;

    @Size(max = 255, message = "Pickup point must not exceed 255 characters")
    private String pickupPoint;

    private String note;

    @NotNull(message = "Departure date is required")
    @Future(message = "Departure date must be in the future")
    private LocalDate departureDate;

    @Min(value = 0, message = "Adults count must be non-negative")
    private Integer adultsCount;

    @Min(value = 0, message = "Children count must be non-negative")
    private Integer childrenCount;

    @Min(value = 0, message = "Babies count must be non-negative")
    private Integer babiesCount;

    @Valid
    @NotEmpty(message = "At least one guest is required")
    private List<BookingGuestRequest> bookingGuestRequests;
}
