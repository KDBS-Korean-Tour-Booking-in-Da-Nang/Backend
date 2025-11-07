package com.example.KDBS.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Data
public class ForumPostRequest {
    @NotBlank(message = "User email is required")
    private String userEmail;
    @NotBlank(message = "Title is required")
    private String title;
    @NotBlank(message = "Content is required")
    private String content;
    private List<MultipartFile> images = new ArrayList<>();
    private List<String> hashtags = new ArrayList<>();
}
