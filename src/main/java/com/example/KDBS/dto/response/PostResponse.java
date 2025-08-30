package com.example.KDBS.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import com.example.KDBS.dto.response.ReactionSummaryResponse;

@Data
public class PostResponse {
    private Long forumPostId;
    private String title;
    private String content;
    private List<PostImgResponse> images;
    private List<HashtagResponse> hashtags;
    private LocalDateTime createdAt;
    private ReactionSummaryResponse reactions;
    private String username;
    private String userAvatar;
}
