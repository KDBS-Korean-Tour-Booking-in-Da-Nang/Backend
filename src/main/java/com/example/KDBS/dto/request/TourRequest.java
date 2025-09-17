package com.example.KDBS.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TourRequest {

    @NotNull
    private String companyEmail;

    @NotBlank
    private String tourName;

    private String tourDescription;
    private String tourDuration;
    private String tourDeparturePoint;
    private String tourVehicle;
    private String tourType;
    private String tourSchedule;
    private Integer amount;

    private BigDecimal adultPrice;
    private BigDecimal childrenPrice;
    private BigDecimal babyPrice;

    // New pricing fields
    private LocalDateTime bookingDeadline;
    private String surcharges; // JSON string from UI

    private List<TourContentRequest> contents;

    @Data
    public static class TourContentRequest {
        private String tourContentTitle; // HTML
        private String tourContentDescription; // HTML
        private List<String> images; // image paths
    }
}
