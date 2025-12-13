package com.example.KDBS.dto.response;

import com.example.KDBS.enums.TourUpdateStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TourUpdateRequestResponse {

    private Long id;
    private Long originalTourId;

    private TourResponse originalTour;
    private TourResponse updatedTour;

    private long bookingCount;
    private List<BookingSimpleResponse> bookings;

    private String companyNote;
    private String staffNote;

    private TourUpdateStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
}
