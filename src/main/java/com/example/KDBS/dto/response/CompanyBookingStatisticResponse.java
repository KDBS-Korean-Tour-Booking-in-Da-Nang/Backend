package com.example.KDBS.dto.response;

import com.example.KDBS.enums.BookingStatus;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompanyBookingStatisticResponse {

    private long totalBookings;

    private Map<BookingStatus, Long> byStatus;
}
