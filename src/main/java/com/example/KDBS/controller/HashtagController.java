package com.example.KDBS.controller;

import com.example.KDBS.dto.response.HashtagStatsResponse;
import com.example.KDBS.service.ForumHashtagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/hashtags")
public class HashtagController {

    @Autowired
    private ForumHashtagService forumHashtagService;

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
