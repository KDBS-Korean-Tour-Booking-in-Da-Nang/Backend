package com.example.KDBS.mapper;

import com.example.KDBS.dto.request.TourRequest;
import com.example.KDBS.dto.response.TourResponse;
import com.example.KDBS.model.Tour;
import com.example.KDBS.model.TourContent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TourMapper {
    // Map Tour entity -> TourResponse DTO
    @Mapping(target = "contents", source = "contents")
    @Mapping(target = "tourStatus", source = "tourStatus")
    @Mapping(target = "companyEmail", ignore = true)
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
    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "tourImgPath", ignore = true)
    @Mapping(target = "bookingDeadline", source = "bookingDeadline", qualifiedByName = "stringToLocalDateTime")
    @Mapping(target = "surcharges", source = "surcharges", qualifiedByName = "surchargesToString")
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
    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "tourImgPath", ignore = true)
    @Mapping(target = "bookingDeadline", source = "bookingDeadline", qualifiedByName = "stringToLocalDateTime")
    @Mapping(target = "surcharges", source = "surcharges", qualifiedByName = "surchargesToString")
    void updateEntityFromRequest(TourRequest request, @MappingTarget Tour tour);

    // Helper methods for mapping new fields

    @Named("surchargesToString")
    default String surchargesToString(List<TourRequest.SurchargeRequest> surcharges) {
        if (surcharges == null || surcharges.isEmpty()) {
            return null;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(surcharges);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    @Named("stringToLocalDateTime")
    default LocalDateTime stringToLocalDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return null;
        }
        try {
            // Handle both "YYYY-MM-DD" and "YYYY-MM-DDTHH:mm" formats
            if (dateTimeString.length() == 10) {
                // Date only: "2024-12-31" -> "2024-12-31T00:00"
                return LocalDateTime.parse(dateTimeString + "T00:00");
            } else {
                // DateTime: "2024-12-31T14:30" -> parse directly
                return LocalDateTime.parse(dateTimeString);
            }
        } catch (Exception e) {
            // Log error and return null for invalid date format
            return null;
        }
    }
}