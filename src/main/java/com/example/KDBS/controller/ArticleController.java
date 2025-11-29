package com.example.KDBS.controller;

import com.example.KDBS.enums.ArticleStatus;
import com.example.KDBS.model.Article;
import com.example.KDBS.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/article")
@RequiredArgsConstructor
public class ArticleController {
    private final ArticleService articleService;

    // Endpoint to trigger crawling articles
    // e.g., GET /api/article/crawl
    @GetMapping("/crawl")
    public ResponseEntity<List<Article>> crawlArticles() {
        List<Article> articles = articleService.crawlArticlesFromDanangXanh();
        return ResponseEntity.ok(articles);
    }

    // Get all
    @GetMapping
    public ResponseEntity<List<Article>> getAllArticles() {
        return ResponseEntity.ok(articleService.getAllArticles());
    }

    // Get by ID
    @GetMapping("/{articleId}")
    public ResponseEntity<Article> getArticleById(@PathVariable Long articleId) {
        return articleService.getArticleById(articleId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    // Get by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Article>> getArticlesByStatus(@PathVariable ArticleStatus status) {
        return ResponseEntity.ok(articleService.getArticlesByStatus(status));
    }

    @PutMapping("/{articleId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<Article> updateStatus(
            @PathVariable Long articleId,
            @RequestParam("status") ArticleStatus status) {
        Article updatedArticle = articleService.updateArticleStatus(articleId, status);
        if (updatedArticle != null) {
            return ResponseEntity.ok(updatedArticle);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
