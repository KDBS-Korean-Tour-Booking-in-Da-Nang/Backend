package com.example.KDBS.mapper;

import com.example.KDBS.dto.request.TourRequest;
import com.example.KDBS.dto.response.TourResponse;
import com.example.KDBS.model.Tour;
import com.example.KDBS.model.TourContent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TourMapper {
    // Map Tour entity -> TourResponse DTO
    @Mapping(target = "id", source = "tourId")
    @Mapping(target = "contents", source = "contents")
    @Mapping(target = "tourStatus", source = "tourStatus")
    TourResponse toResponse(Tour tour);

    // Explicit mapping for TourContent to TourContentResponse
    @Mapping(target = "tourContentTitle", source = "tourContentTitle")
    @Mapping(target = "tourContentDescription", source = "tourContentDescription")
    TourResponse.TourContentResponse toContentResponse(TourContent content);

    // Request -> Entity (for create)
    @Mapping(target = "tourId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "tourStatus", constant = "NOT_APPROVED")
    @Mapping(target = "contents", ignore = true)
    @Mapping(target = "ratings", ignore = true)
    Tour toEntity(TourRequest request);

    // Map nested request -> entity
    @Mapping(target = "tourContentId", ignore = true)
    @Mapping(target = "tour", ignore = true)
    @Mapping(target = "images", ignore = true)
    TourContent toContentEntity(TourRequest.TourContentRequest req);

    // Update entity from request
    @Mapping(target = "tourId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "tourStatus", ignore = true)
    @Mapping(target = "contents", ignore = true)
    @Mapping(target = "ratings", ignore = true)
    void updateEntityFromRequest(TourRequest request, @MappingTarget Tour tour);
}