package com.example.KDBS.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Data
public class ForumPostRequest {
    private String userEmail;
    private String title;
    private String content;
    private List<MultipartFile> images = new ArrayList<>();
    private List<String> hashtags = new ArrayList<>();
}
