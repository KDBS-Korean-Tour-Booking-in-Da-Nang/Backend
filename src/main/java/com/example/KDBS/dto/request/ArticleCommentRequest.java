package com.example.KDBS.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ArticleCommentRequest {

    @NotBlank(message = "User email is required")
    @Email(message = "Invalid email format")
    private String userEmail;

    @NotNull(message = "Article ID is required")
    private Long articleId;

    @NotBlank(message = "Content is required")
    private String content;

    private String imgPath; // optional

    private Long parentCommentId; // optional
}
