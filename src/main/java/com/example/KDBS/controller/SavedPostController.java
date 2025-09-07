package com.example.KDBS.controller;

import com.example.KDBS.dto.request.SavePostRequest;
import com.example.KDBS.dto.response.ApiResponse;
import com.example.KDBS.dto.response.SavedPostResponse;
import com.example.KDBS.service.SavedPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/saved-posts")
@RequiredArgsConstructor
@CrossOrigin("*")
public class SavedPostController {

        private final SavedPostService savedPostService;

        // save post
        @PostMapping("/save")
        public ApiResponse<SavedPostResponse> savePost(
                        @RequestBody @Valid SavePostRequest request,
                        @RequestHeader("User-Email") String userEmail) {

                SavedPostResponse response = savedPostService.savePost(request, userEmail);

                return ApiResponse.<SavedPostResponse>builder()
                                .result(response)
                                .message("Post saved successfully")
                                .build();
        }

        // unsave post
        @DeleteMapping("/unsave/{postId}")
        public ApiResponse<Void> unsavePost(
                        @PathVariable Long postId,
                        @RequestHeader("User-Email") String userEmail) {

                savedPostService.unsavePost(postId, userEmail);

                return ApiResponse.<Void>builder()
                                .message("Post unsaved successfully")
                                .build();
        }

        // get danh sach post da save
        @GetMapping("/my-saved")
        public ApiResponse<List<SavedPostResponse>> getMySavedPosts(
                        @RequestHeader("User-Email") String userEmail) {

                List<SavedPostResponse> savedPosts = savedPostService.getSavedPostsByUser(userEmail);

                return ApiResponse.<List<SavedPostResponse>>builder()
                                .result(savedPosts)
                                .build();
        }

        // check post da save hay chua -> UI
        @GetMapping("/check/{postId}")
        public ApiResponse<Boolean> isPostSavedByUser(
                        @PathVariable Long postId,
                        @RequestHeader("User-Email") String userEmail) {

                boolean isSaved = savedPostService.isPostSavedByUser(postId, userEmail);

                return ApiResponse.<Boolean>builder()
                                .result(isSaved)
                                .build();
        }

        // get so luong save cua 1 post
        @GetMapping("/count/{postId}")
        public ApiResponse<Long> getSaveCountByPost(@PathVariable Long postId) {
                Long saveCount = savedPostService.getSaveCountByPost(postId);

                return ApiResponse.<Long>builder()
                                .result(saveCount)
                                .build();
        }

        // get so luong save cua user -> UI
        @GetMapping("/my-count")
        public ApiResponse<Long> getMySaveCount(
                        @RequestHeader("User-Email") String userEmail) {

                Long saveCount = savedPostService.getSaveCountByUser(userEmail);

                return ApiResponse.<Long>builder()
                                .result(saveCount)
                                .build();
        }

        // get danh sach user da save post
        @GetMapping("/users/{postId}")
        public ApiResponse<List<String>> getUsersWhoSavedPost(@PathVariable Long postId) {
                List<String> usernames = savedPostService.getUsersWhoSavedPost(postId);

                return ApiResponse.<List<String>>builder()
                                .result(usernames)
                                .build();
        }
}
