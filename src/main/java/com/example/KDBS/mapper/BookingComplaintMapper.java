package com.example.KDBS.mapper;

import com.example.KDBS.dto.response.BookingComplaintResponse;
import com.example.KDBS.model.BookingComplaint;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface BookingComplaintMapper {
    BookingComplaintResponse toBookingComplaintResponse(BookingComplaint bookingComplaint);
}
