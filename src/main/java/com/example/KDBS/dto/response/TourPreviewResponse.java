package com.example.KDBS.dto.response;

import lombok.Data;

@Data
public class TourPreviewResponse {
    private Long tourId;
    private String tourName;
    private String tourDescription;
    private String tourImgPath;
    private String tourUrl;
}
