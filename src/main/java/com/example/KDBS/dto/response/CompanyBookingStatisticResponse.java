package com.example.KDBS.dto.response;

import com.example.KDBS.enums.BookingStatus;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class CompanyBookingStatisticResponse {

    private long totalBookings;

    private Map<BookingStatus, Long> byStatus;
}
