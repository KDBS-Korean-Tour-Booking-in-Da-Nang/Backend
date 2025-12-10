package com.example.KDBS.mapper;

import com.example.KDBS.dto.response.BookingSimpleResponse;
import com.example.KDBS.model.Booking;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookingSimpleMapper {

    @Mapping(target = "status", expression = "java(b.getBookingStatus().name())")
    BookingSimpleResponse toSimple(Booking b);
}
