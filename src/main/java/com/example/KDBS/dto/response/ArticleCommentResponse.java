package com.example.KDBS.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleCommentResponse {

    private Long articleCommentId;
    private String content;
    private String imgPath;
    private Integer react;
    private LocalDateTime createdAt;

    private String username;
    private String userAvatar;
    private String userEmail;

    private Long articleId;
    private Long parentCommentId;
}
