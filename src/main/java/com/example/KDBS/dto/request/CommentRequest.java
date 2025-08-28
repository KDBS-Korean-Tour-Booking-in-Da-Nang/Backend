package com.example.KDBS.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CommentRequest {
    private Long postId;
    private String userEmail;
    private String content;
    private MultipartFile imgPath;
}
