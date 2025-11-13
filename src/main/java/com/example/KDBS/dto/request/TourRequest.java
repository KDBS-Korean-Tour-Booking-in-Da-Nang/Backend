package com.example.KDBS.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class TourRequest {

    @NotBlank(message = "User email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String companyEmail;

    @NotBlank(message = "Tour name is required")
    private String tourName;

    @NotBlank(message = "Tour description is required")
    private String tourDescription;
    @NotBlank(message = "Tour location is required")
    private String tourDuration;
    @NotNull(message = "Tour integer duration is required")
    private int tourIntDuration;
    @NotBlank(message = "Tour departure point is required")
    private String tourDeparturePoint;
    @NotNull(message = "Tour expiration date is required")
    private LocalDate tourExpirationDate;
    @NotNull(message = "Tour deadline is required")
    private int tourDeadline;
    @NotBlank(message = "Tour vehicle is required")
    private String tourVehicle;
    @NotBlank(message = "Tour type is required")
    private String tourType;
    @NotBlank(message = "Tour schedule is required")
    private String tourSchedule;
    @NotNull(message = "Tour amount is required")
    private int amount;

    @NotNull(message = "Adult price is required")
    private BigDecimal adultPrice;
    private BigDecimal childrenPrice;
    private BigDecimal babyPrice;

    private List<TourContentRequest> contents;

    @Data
    public static class TourContentRequest {
        private String tourContentTitle; // HTML
        private String tourContentDescription; // HTML
        private List<String> images; // image paths
        // Presentation preferences from wizard Step 2
        private String dayColor; // e.g. #10b981
        private String titleAlignment; // left | center | right
    }
}
