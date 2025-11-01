package com.example.KDBS.controller;

import com.example.KDBS.dto.response.HashtagStatsResponse;
import com.example.KDBS.service.ForumHashtagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/hashtags")
@RequiredArgsConstructor
public class HashtagController {
    private final ForumHashtagService forumHashtagService;

    @GetMapping("/popular")
    public ResponseEntity<List<HashtagStatsResponse>> getPopularHashtags(
            @RequestParam(defaultValue = "10") int limit) {
        List<HashtagStatsResponse> popularHashtags = forumHashtagService.getPopularHashtags(limit);
        return ResponseEntity.ok(popularHashtags);
    }

    @GetMapping("/search")
    public ResponseEntity<List<HashtagStatsResponse>> searchHashtags(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") int limit) {
        List<HashtagStatsResponse> hashtags = forumHashtagService.searchHashtags(keyword, limit);
        return ResponseEntity.ok(hashtags);
    }
}
