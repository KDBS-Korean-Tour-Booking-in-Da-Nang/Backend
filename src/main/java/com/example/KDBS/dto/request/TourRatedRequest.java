package com.example.KDBS.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class TourRatedRequest {
    @NotNull(message = "Tour ID is required")
    private Long tourId;     // tour cần đánh giá
    @NotBlank(message = "User email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String userEmail;     // user đánh giá
    @NotNull(message = "Star rating is required")
    @Size(min = 1, max = 5, message = "Star rating must be between 1 and 5")
    private Integer star;    // số sao (1–5)
    private String comment;  // nội dung review
}
