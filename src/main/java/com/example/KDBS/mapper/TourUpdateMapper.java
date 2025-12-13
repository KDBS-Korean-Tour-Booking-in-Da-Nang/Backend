package com.example.KDBS.mapper;

import com.example.KDBS.dto.request.TourRequest;
import com.example.KDBS.dto.response.TourResponse;
import com.example.KDBS.dto.response.TourUpdateRequestResponse;
import com.example.KDBS.model.Tour;
import com.example.KDBS.model.TourUpdateRequest;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        uses = { TourMapper.class },
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TourUpdateMapper {

    @Mapping(target = "originalTourId", source = "originalTour.tourId")
    @Mapping(target = "originalTour", source = "originalTour")
    @Mapping(target = "updatedTour", ignore = true)
    @Mapping(target = "bookingCount", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    TourUpdateRequestResponse toResponse(TourUpdateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tourImgPath", ignore = true)
    @Mapping(target = "tourStatus", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "contents", source = "contents")
    TourResponse toTourResponse(TourRequest tourRequest);

    TourResponse.TourContentResponse toTourContentResponse(TourRequest.TourContentRequest contentRequest);


    // Staff approve → apply updated fields vào Tour thật
    @Mapping(target = "tourId", ignore = true)
    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "tourImgPath", ignore = true)
    @Mapping(target = "tourStatus", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "ratings", ignore = true)
    void applyUpdateToTour(TourRequest updated, @MappingTarget Tour original);
}
