package com.example.KDBS.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Data
public class TourRatedRequest {
    private Long tourId;     // tour cần đánh giá
    private String userEmail;     // user đánh giá
    private Integer star;    // số sao (1–5)
    private String comment;  // nội dung review
}
