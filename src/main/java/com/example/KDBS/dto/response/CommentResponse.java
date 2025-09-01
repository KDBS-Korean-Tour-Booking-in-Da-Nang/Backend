package com.example.KDBS.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentResponse {
    private Long forumCommentId;
    private String content;
    private String imgPath;
    private Integer react;
    private LocalDateTime createdAt;
}