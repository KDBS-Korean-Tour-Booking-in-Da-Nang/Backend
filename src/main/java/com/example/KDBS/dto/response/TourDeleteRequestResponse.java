package com.example.KDBS.dto.response;

import com.example.KDBS.enums.BookingStatus;
import com.example.KDBS.enums.TourDeleteStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TourDeleteRequestResponse {

    private Long id;
    private Long tourId;

    private String tourName;

    private long bookingCount;
    private List<BookingSimpleResponse> bookings;

    private String companyNote;
    private String staffNote;

    private TourDeleteStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
}

