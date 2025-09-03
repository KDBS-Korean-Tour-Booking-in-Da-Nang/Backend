package com.example.KDBS.controller;

import com.example.KDBS.dto.request.ReactionRequest;
import com.example.KDBS.dto.response.ReactionResponse;
import com.example.KDBS.dto.response.ReactionSummaryResponse;
import com.example.KDBS.enums.ReactionTargetType;
import com.example.KDBS.service.ReactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("api/reactions")
public class ReactionController {

    @Autowired
    private ReactionService reactionService;

    @PostMapping
    public ResponseEntity<ReactionResponse> createReaction(@RequestBody ReactionRequest reactionRequest) {
        ReactionResponse response = reactionService.createReaction(reactionRequest);
        return ResponseEntity.ok(response);
    }

    // Compatibility with the provided API spec
    @PostMapping("/add")
    public ResponseEntity<ReactionResponse> addReactionLegacy(
            @RequestHeader(value = "User-Email", required = false) String userEmail,
            @RequestBody ReactionRequest body) {
        // Fallbacks to avoid 401/Unauthenticated when header is missing
        if (userEmail == null || userEmail.isBlank()) {
            if (body.getUserEmail() != null && !body.getUserEmail().isBlank()) {
                userEmail = body.getUserEmail();
            } else {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getName() != null) {
                    userEmail = auth.getName();
                }
            }
        }
        ReactionResponse response = reactionService.addOrUpdateReaction(body, userEmail);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> removeReaction(@RequestBody ReactionRequest reactionRequest) {
        reactionService.removeReactionByRequest(reactionRequest);
        return ResponseEntity.noContent().build();
    }

    // Compatibility remove endpoint as spec: POST
    // /api/reactions/{targetType}/{targetId}
    @PostMapping("/{targetType}/{targetId}")
    public ResponseEntity<Void> removeReactionLegacy(
            @PathVariable ReactionTargetType targetType,
            @PathVariable Long targetId,
            @RequestHeader(value = "User-Email", required = false) String userEmail,
            @RequestParam(value = "userEmail", required = false) String userEmailQuery) {
        if (userEmail == null || userEmail.isBlank()) {
            userEmail = userEmailQuery;
        }
        reactionService.removeReaction(targetId, targetType, userEmail);
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
}
