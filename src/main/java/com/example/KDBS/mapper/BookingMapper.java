package com.example.KDBS.mapper;

import com.example.KDBS.dto.request.BookingRequest;
import com.example.KDBS.dto.response.BookingResponse;
import com.example.KDBS.model.Booking;
import com.example.KDBS.model.BookingGuest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    BookingMapper INSTANCE = Mappers.getMapper(BookingMapper.class);

    @Mapping(target = "bookingId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "guests", ignore = true)
    Booking toEntity(BookingRequest request);

    BookingResponse toResponse(Booking booking);

    @Mapping(target = "guestId", ignore = true)
    @Mapping(target = "bookingId", source = "bookingId")
    @Mapping(target = "booking", ignore = true)
    BookingGuest toGuestEntity(BookingRequest.GuestRequest guestRequest, Long bookingId);
}
