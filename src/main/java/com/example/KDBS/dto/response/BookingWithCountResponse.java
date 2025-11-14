package com.example.KDBS.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BookingWithCountResponse {
    private int count;
    private List<BookingResponse> bookings;

    public BookingWithCountResponse(List<BookingResponse> bookings) {
        this.count = bookings.size();
        this.bookings = bookings;
    }
}
