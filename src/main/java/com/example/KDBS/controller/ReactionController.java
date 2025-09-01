package com.example.KDBS.controller;

import com.example.KDBS.dto.request.ReactionRequest;
import com.example.KDBS.dto.response.ApiResponse;
import com.example.KDBS.dto.response.ReactionResponse;
import com.example.KDBS.dto.response.ReactionSummaryResponse;
import com.example.KDBS.enums.ReactionTargetType;
import com.example.KDBS.service.ReactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reactions")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ReactionController {

    private final ReactionService reactionService;

    @PostMapping("/add")
    public ApiResponse<ReactionResponse> addOrUpdateReaction(
            @RequestBody @Valid ReactionRequest request,
            @RequestHeader("User-Email") String userEmail) {
        
        ReactionResponse response = reactionService.addOrUpdateReaction(request, userEmail);
        
        if (response == null) {
            return ApiResponse.<ReactionResponse>builder()
                    .message("Reaction removed successfully")
                    .build();
        }
        
        return ApiResponse.<ReactionResponse>builder()
                .result(response)
                .message("Reaction added/updated successfully")
                .build();
    }

    @DeleteMapping("/{targetType}/{targetId}")
    public ApiResponse<Void> removeReaction(
            @PathVariable ReactionTargetType targetType,
            @PathVariable Long targetId,
            @RequestHeader("User-Email") String userEmail) {
        
        reactionService.removeReaction(targetId, targetType, userEmail);
        
        return ApiResponse.<Void>builder()
                .message("Reaction removed successfully")
                .build();
    }

    @GetMapping("/{targetType}/{targetId}/summary")
    public ApiResponse<ReactionSummaryResponse> getReactionSummary(
            @PathVariable ReactionTargetType targetType,
            @PathVariable Long targetId,
            @RequestHeader(value = "User-Email", required = false) String userEmail) {
        
        ReactionSummaryResponse summary = reactionService.getReactionSummary(targetId, targetType, userEmail);
        
        return ApiResponse.<ReactionSummaryResponse>builder()
                .result(summary)
                .build();
    }

    // dashboard for admin
    @GetMapping("/{targetType}/{targetId}/list")
    public ApiResponse<List<ReactionResponse>> getReactionsByTarget(
            @PathVariable ReactionTargetType targetType,
            @PathVariable Long targetId) {
        
        List<ReactionResponse> reactions = reactionService.getReactionsByTarget(targetId, targetType);
        
        return ApiResponse.<List<ReactionResponse>>builder()
                .result(reactions)
                .build();
    }
}
