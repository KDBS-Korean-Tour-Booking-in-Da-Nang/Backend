package com.example.KDBS.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TourRatedResponse {
    private Long id;
    private Integer star;
    private String comment;
    private LocalDateTime createdAt;
}
