package com.example.KDBS.dto.request;

import lombok.Data;

@Data
public class CommentRequest {
    private String userEmail;
    private Long forumPostId;
    private String content;
    private String imgPath;
    private Long parentCommentId; // optional: when replying to a comment
}
