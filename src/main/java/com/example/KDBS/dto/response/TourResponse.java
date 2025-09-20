package com.example.KDBS.dto.response;

import com.example.KDBS.enums.TourStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TourResponse {
    private Long id;
    private String tourName;
    private String tourDescription;
    private String tourImgPath;
    private String tourDuration;
    private String tourDeparturePoint;
    private String tourVehicle;
    private String tourType;
    private String tourSchedule;
    private Integer amount;
    private BigDecimal adultPrice;
    private BigDecimal childrenPrice;
    private BigDecimal babyPrice;
    private TourStatus tourStatus;
    private LocalDateTime createdAt;
    private List<TourContentResponse> contents;

    @Data
    public static class TourContentResponse {
        private String tourContentTitle;
        private String tourContentDescription;
        private List<String> images;
        private String dayColor;
        private String titleAlignment;
    }
}
