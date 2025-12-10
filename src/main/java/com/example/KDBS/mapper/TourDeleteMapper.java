package com.example.KDBS.mapper;

import com.example.KDBS.dto.response.TourDeleteRequestResponse;
import com.example.KDBS.model.TourDeleteRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TourDeleteMapper {

    @Mapping(target = "tourId", source = "tour.tourId")
    @Mapping(target = "tourName", source = "tour.tourName")
    @Mapping(target = "bookingCount", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    TourDeleteRequestResponse toResponse(TourDeleteRequest request);
}
