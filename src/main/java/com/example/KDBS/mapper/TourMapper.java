package com.example.KDBS.mapper;

import com.example.KDBS.dto.request.TourRequest;
import com.example.KDBS.dto.response.TourResponse;
import com.example.KDBS.model.Tour;
import com.example.KDBS.model.TourContent;
import com.example.KDBS.model.TourContentImg;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TourMapper {
    // Map Tour entity -> TourResponse DTO
    @Mapping(target = "id", source = "tourId")
    @Mapping(target = "contents", source = "contents")
    @Mapping(target = "tourStatus", source = "tourStatus")
    TourResponse toTourResponse(Tour tour);

    // Explicit mapping for TourContent to TourContentResponse
    @Mapping(target = "tourContentTitle", source = "tourContentTitle")
    @Mapping(target = "tourContentDescription", source = "tourContentDescription")
    @Mapping(target = "dayColor", source = "dayColor")
    @Mapping(target = "titleAlignment", source = "titleAlignment")
    @Mapping(target = "images", source = "images", qualifiedByName = "mapImages")
    TourResponse.TourContentResponse toContentResponse(TourContent content);

    @Named("mapImages")
    default List<String> mapImages(List<TourContentImg> images) {
        if (images == null)
            return null;
        return images.stream().map(TourContentImg::getImgPath).collect(Collectors.toList());
    }

    // Request -> Entity (for create)
    @Mapping(target = "tourId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "tourStatus", constant = "NOT_APPROVED")
    @Mapping(target = "contents", ignore = true)
    @Mapping(target = "ratings", ignore = true)
    Tour toTour(TourRequest request);

    // Map nested request -> entity
    @Mapping(target = "tourContentId", ignore = true)
    @Mapping(target = "tour", ignore = true)
    @Mapping(target = "images", ignore = true)
    TourContent toTourContent(TourRequest.TourContentRequest req);

    // Update entity from request
    @Mapping(target = "tourId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "tourStatus", ignore = true)
    @Mapping(target = "contents", ignore = true)
    @Mapping(target = "ratings", ignore = true)
    void updateTourFromRequest(TourRequest request, @MappingTarget Tour tour);
}