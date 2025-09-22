package com.example.KDBS.controller;

import com.example.KDBS.dto.request.ForumCommentRequest;
import com.example.KDBS.dto.response.ForumCommentResponse;
import com.example.KDBS.service.ForumCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("api/comments")
public class ForumCommentController {

    @Autowired
    private ForumCommentService forumCommentService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ForumCommentResponse> createComment(@RequestBody ForumCommentRequest forumCommentRequest)
            throws IOException {
        ForumCommentResponse response = forumCommentService.createComment(forumCommentRequest);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ForumCommentResponse> updateComment(
            @PathVariable Long id,
            @RequestBody ForumCommentRequest updateRequest) throws IOException {
        ForumCommentResponse response = forumCommentService.updateComment(id, updateRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated() and @forumCommentSecurity.canDeleteComment(#id, #userEmail)")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long id,
            @RequestParam String userEmail) {
        forumCommentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<ForumCommentResponse>> getCommentsByPost(@PathVariable Long postId) {
        List<ForumCommentResponse> responses = forumCommentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{commentId}/replies")
    public ResponseEntity<List<ForumCommentResponse>> getReplies(@PathVariable Long commentId) {
        List<ForumCommentResponse> responses = forumCommentService.getReplies(commentId);
        return ResponseEntity.ok(responses);
    }
}
