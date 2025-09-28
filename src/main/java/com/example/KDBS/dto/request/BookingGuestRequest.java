package com.example.KDBS.dto.request;

import com.example.KDBS.enums.Gender;
import com.example.KDBS.enums.BookingGuestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingGuestRequest {
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
    private BookingGuestType bookingGuestType;
}
