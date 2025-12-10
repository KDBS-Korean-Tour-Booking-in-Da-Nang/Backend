package com.example.KDBS.dto.request;

import jakarta.validation.Valid;
import lombok.Data;

@Data
public class TourUpdateCreateRequest {
    @Valid
    private TourRequest updatedTour;

    private String note;
}
