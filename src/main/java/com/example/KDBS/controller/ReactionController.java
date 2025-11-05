package com.example.KDBS.controller;

import com.example.KDBS.dto.request.ReactionRequest;
import com.example.KDBS.dto.response.ReactionResponse;
import com.example.KDBS.dto.response.ReactionSummaryResponse;
import com.example.KDBS.enums.ReactionTargetType;
import com.example.KDBS.service.ReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/reactions")
@RequiredArgsConstructor
public class ReactionController {
    private final ReactionService reactionService;

    // Compatibility with the provided API spec
    @PostMapping("/add")
    public ResponseEntity<ReactionResponse> addReactionLegacy(
            @RequestBody ReactionRequest request) {

        ReactionResponse response = reactionService.addOrUpdateReaction(request);
        return ResponseEntity.ok(response);
    }

    // Compatibility remove endpoint as spec: POST
    // /api/reactions/{targetType}/{targetId}
    @PostMapping("/delete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeReactionLegacy(
            @RequestBody ReactionRequest request) {
        reactionService.removeReaction(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/post/{postId}/count")
    public ResponseEntity<Long> getReactionCountByPost(@PathVariable Long postId) {
        Long count = reactionService.getReactionCountByPost(postId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/post/{postId}/user/{userEmail}")
    public ResponseEntity<Boolean> hasUserReacted(@PathVariable Long postId, @PathVariable String userEmail) {
        Boolean hasReacted = reactionService.hasUserReacted(postId, userEmail);
        return ResponseEntity.ok(hasReacted);
    }

    @GetMapping("/post/{postId}/summary")
    public ResponseEntity<ReactionSummaryResponse> getPostReactionSummary(
            @PathVariable Long postId,
            @RequestParam(required = false) String userEmail) {
        ReactionSummaryResponse summary = reactionService.getReactionSummary(postId, ReactionTargetType.POST,
                userEmail);
        return ResponseEntity.ok(summary);
    }

    // Image-specific endpoints removed (preview-only on FE)

    // Comment reaction summary
    @GetMapping("/comment/{commentId}/summary")
    public ResponseEntity<ReactionSummaryResponse> getCommentReactionSummary(
            @PathVariable Long commentId,
            @RequestParam(required = false) String userEmail) {
        ReactionSummaryResponse summary = reactionService.getReactionSummary(commentId, ReactionTargetType.COMMENT,
                userEmail);
        return ResponseEntity.ok(summary);
    }

    // Get user's reactions by reaction type (LIKE, DISLIKE)
    @GetMapping("/user/{userEmail}")
    public ResponseEntity<List<ReactionResponse>> getUserReactions(
            @PathVariable String userEmail,
            @RequestParam(required = false) String reactionType) {
        List<ReactionResponse> reactions = reactionService.getUserReactions(userEmail, reactionType);
        return ResponseEntity.ok(reactions);
    }

    // Get user's reactions by target type (POST, COMMENT, IMG) - for backward
    // compatibility
    @GetMapping("/user/{userEmail}/by-target")
    public ResponseEntity<List<ReactionResponse>> getUserReactionsByTargetType(
            @PathVariable String userEmail,
            @RequestParam(required = false) String targetType) {
        ReactionTargetType type = targetType != null ? ReactionTargetType.valueOf(targetType) : null;
        List<ReactionResponse> reactions = reactionService.getUserReactionsByTargetType(userEmail, type);
        return ResponseEntity.ok(reactions);
    }
}
