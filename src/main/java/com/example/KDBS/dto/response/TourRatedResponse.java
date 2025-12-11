package com.example.KDBS.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TourRatedResponse {
    private Long tourRatedId;
    private Long userId;
    private String username;
    private String userAvatar;

    private Long tourId;
    private Integer star;
    private String comment;
    private LocalDateTime createdAt;
}