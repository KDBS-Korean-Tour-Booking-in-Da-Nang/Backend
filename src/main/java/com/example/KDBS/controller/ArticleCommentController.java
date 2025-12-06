package com.example.KDBS.controller;

import com.example.KDBS.dto.request.ArticleCommentRequest;
import com.example.KDBS.dto.response.ArticleCommentResponse;
import com.example.KDBS.service.ArticleCommentService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("api/article-comments")
@RequiredArgsConstructor
public class ArticleCommentController {

    private final ArticleCommentService service;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ArticleCommentResponse> createComment(
            @RequestBody @Valid ArticleCommentRequest req) {
        return ResponseEntity.ok(service.createComment(req));
    }

    @GetMapping("/article/{articleId}")
    public ResponseEntity<List<ArticleCommentResponse>> getComments(@PathVariable Long articleId) {
        return ResponseEntity.ok(service.getCommentsByArticleId(articleId));
    }

    @GetMapping("/{commentId}/replies")
    public ResponseEntity<List<ArticleCommentResponse>> getReplies(@PathVariable Long commentId) {
        return ResponseEntity.ok(service.getReplies(commentId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ArticleCommentResponse> updateComment(
            @PathVariable Long id,
            @RequestBody @Valid ArticleCommentRequest req) {
        return ResponseEntity.ok(service.updateComment(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long id,
            @RequestParam String userEmail) {
        service.deleteComment(id, userEmail);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticleCommentResponse> getCommentById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getCommentById(id));
    }
}
