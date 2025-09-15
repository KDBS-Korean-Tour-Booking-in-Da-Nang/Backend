package com.example.KDBS.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
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

    // Additional fields from FE wizard
    private List<String> availableDates;        // Available tour dates
    private String bookingDeadline;             // Booking deadline
    private String surchargePolicy;             // Surcharge policy (HTML)
    private String cancellationPolicy;          // Cancellation policy (HTML)
    private List<SurchargeRequest> surcharges;  // Surcharge details
    private List<String> gallery;               // Gallery image paths
    private List<String> attachments;           // Attachment file paths

    private List<TourContentRequest> contents;

    @Data
    public static class TourContentRequest {
        private String tourContentTitle;      // HTML
        private String tourContentDescription; // HTML
        private List<String> images;          // image paths
    }

    @Data
    public static class SurchargeRequest {
        private String type;        // holiday, weekend, single-room, etc.
        private String name;        // Surcharge name
        private String percentage;  // Percentage surcharge
        private String description; // HTML description
    }
}
