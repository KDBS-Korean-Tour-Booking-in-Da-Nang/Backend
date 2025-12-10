package com.example.KDBS.mapper;

import com.example.KDBS.dto.request.TourRequest;
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
    TourUpdateRequestResponse toResponse(TourUpdateRequest request);


    // Staff approve → apply updated fields vào Tour thật
    @BeanMapping(ignoreByDefault = false)
    void applyUpdateToTour(TourRequest updated, @MappingTarget Tour original);
}
