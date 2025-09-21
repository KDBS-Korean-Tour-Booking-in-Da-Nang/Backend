package com.example.KDBS.mapper;

import com.example.KDBS.dto.request.TourRatedRequest;
import com.example.KDBS.dto.response.TourRatedResponse;
import com.example.KDBS.model.TourRated;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TourRatedMapper {

    // Request -> Entity
    @Mapping(target = "tourRatedId", ignore = true)
    @Mapping(target = "tour", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    TourRated toTourRated(TourRatedRequest request);

    // Entity -> Response
    @Mapping(target = "id", source = "tourRatedId")
    TourRatedResponse toTourRatedResponse(TourRated entity);

    // Update
    @Mapping(target = "tourRatedId", ignore = true)
    @Mapping(target = "tour", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateTourRatedFromRequest(TourRatedRequest request, @MappingTarget TourRated entity);


}
