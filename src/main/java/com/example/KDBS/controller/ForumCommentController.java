package com.example.KDBS.controller;

import com.example.KDBS.dto.request.ForumCommentRequest;
import com.example.KDBS.dto.response.ForumCommentResponse;
import com.example.KDBS.service.ForumCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/comments")
@RequiredArgsConstructor
public class ForumCommentController {
    private final ForumCommentService forumCommentService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ForumCommentResponse> createComment(@RequestBody ForumCommentRequest forumCommentRequest) {
        ForumCommentResponse response = forumCommentService.createComment(forumCommentRequest);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ForumCommentResponse> updateComment(
            @PathVariable Long id,
            @RequestBody ForumCommentRequest updateRequest){
        ForumCommentResponse response = forumCommentService.updateComment(id, updateRequest);
        return ResponseEntity.ok(response);
    }

    //use userEmail in PreAuthorize to check if the user can delete the comment
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long id,
            @RequestParam String userEmail) {
        forumCommentService.deleteComment(id, userEmail);
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

    @GetMapping("/{id}")
    public ResponseEntity<ForumCommentResponse> getCommentById(@PathVariable Long id) {
        return ResponseEntity.ok(forumCommentService.getCommentById(id));
    }
}
