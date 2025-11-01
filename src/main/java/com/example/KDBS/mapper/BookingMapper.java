package com.example.KDBS.mapper;

import com.example.KDBS.dto.request.BookingGuestRequest;
import com.example.KDBS.dto.request.BookingRequest;
import com.example.KDBS.dto.response.BookingGuestResponse;
import com.example.KDBS.dto.response.BookingResponse;
import com.example.KDBS.dto.response.BookingSummaryResponse;
import com.example.KDBS.dto.response.GuestInsuranceResponse;
import com.example.KDBS.enums.InsuranceStatus;
import com.example.KDBS.model.Booking;
import com.example.KDBS.model.BookingGuest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface BookingMapper {

    // ----- Booking -----
    @Mapping(target = "bookingId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "guests", ignore = true) // sẽ set riêng
    @Mapping(target = "tour", ignore = true)   // tránh vòng lặp
    Booking toBooking(BookingRequest request);

    BookingResponse toBookingResponse(Booking booking);

    // ----- BookingGuest -----
    @Mapping(target = "bookingGuestId", ignore = true)
    @Mapping(target = "booking", ignore = true) // set riêng từ service
    BookingGuest toBookingGuest(BookingGuestRequest request);

    BookingGuestResponse toBookingGuestResponse(BookingGuest guest);

    List<BookingGuestResponse> toBookingGuestResponses(List<BookingGuest> guests);

    // ----- BookingSummary -----
    @Mapping(target = "tourName", expression = "java(tourName)")
    @Mapping(target = "totalAmount", expression = "java(totalAmount)")
    @Mapping(target = "status", constant = "PENDING")
    BookingSummaryResponse toBookingSummaryResponse(Booking booking, String tourName, BigDecimal totalAmount);

    GuestInsuranceResponse toGuestInsuranceResponse(
            BookingGuestResponse guestResponse,
            String insuranceNumber,
            InsuranceStatus insuranceStatus
    );

}
