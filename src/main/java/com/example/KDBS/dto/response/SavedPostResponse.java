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
public class SavedPostResponse {

    private Long savedPostId;
    private Long postId;
    private String postTitle;
    private String postContent;
    private String postAuthor;
    private String postAuthorAvatar;
    private LocalDateTime postCreatedAt;
    private String note;
    private LocalDateTime savedAt;
}
