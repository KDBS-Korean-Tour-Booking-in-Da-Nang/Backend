package com.example.KDBS.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private Long forumCommentId;
    private String content;
    private String imgPath;
    private Integer react;
    private LocalDateTime createdAt;
    private String username;
    private String userAvatar;
    private String userEmail;
    private Integer userId;
    private Long forumPostId;
    private Long parentCommentId;
}