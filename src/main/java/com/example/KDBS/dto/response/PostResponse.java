package com.example.KDBS.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostResponse {
    private Long forumPostId;
    private String title;
    private String content;
    private List<PostImgResponse> images;
    private List<HashtagResponse> hashtags;
    private LocalDateTime createdAt;
    private ReactionSummaryResponse reactions;
    private Long saveCount;
    private String username;
    private String userAvatar;
    private String userEmail;
}
