package com.example.KDBS.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ForumCommentRequest {
    @NotBlank(message = "User email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must be at most 100 characters")
    private String userEmail;
    @NotNull(message = "Forum post ID is required")
    private Long forumPostId;
    @NotBlank(message = "Content is required")
    private String content;
    @NotBlank(message = "Image path is required")
    private String imgPath;
    private Long parentCommentId; // optional: when replying to a comment
}
