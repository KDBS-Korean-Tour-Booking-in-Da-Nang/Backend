package com.example.KDBS.controller;

import com.example.KDBS.dto.request.CommentRequest;
import com.example.KDBS.dto.response.CommentResponse;
import com.example.KDBS.service.ForumCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/comments")
public class ForumCommentController {

    @Autowired
    private ForumCommentService forumCommentService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponse> createComment(@RequestBody CommentRequest commentRequest) {
        CommentResponse response = forumCommentService.createComment(commentRequest);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long id,
            @RequestBody CommentRequest updateRequest) {
        CommentResponse response = forumCommentService.updateComment(id, updateRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long id,
            @RequestParam String userEmail) {
        forumCommentService.deleteComment(id, userEmail);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByPost(@PathVariable Long postId) {
        List<CommentResponse> responses = forumCommentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{commentId}/replies")
    public ResponseEntity<List<CommentResponse>> getReplies(@PathVariable Long commentId) {
        List<CommentResponse> responses = forumCommentService.getReplies(commentId);
        return ResponseEntity.ok(responses);
    }
}
