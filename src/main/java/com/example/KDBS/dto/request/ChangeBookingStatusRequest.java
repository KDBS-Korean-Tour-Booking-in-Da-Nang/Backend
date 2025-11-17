package com.example.KDBS.dto.request;

import com.example.KDBS.enums.BookingStatus;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class ChangeBookingStatusRequest {
    private BookingStatus status;
    private String message;
}
