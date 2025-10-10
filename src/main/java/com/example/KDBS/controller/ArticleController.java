package com.example.KDBS.controller;

import com.example.KDBS.enums.ArticleStatus;
import com.example.KDBS.model.Article;
import com.example.KDBS.service.ArticleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/article")
public class ArticleController {
    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    // Endpoint to trigger crawling articles
    // e.g., GET /api/article/crawl
    @RequestMapping("/crawl")
    public ResponseEntity<List<Article>> crawlArticles() {
        List<Article> articles = articleService.crawlArticlesFromDanangXanh();
        return ResponseEntity.ok(articles);
    }

    // Get all
    @GetMapping
    public List<Article> getAllArticles() {
        return articleService.getAllArticles();
    }

    // Get by ID
    @GetMapping("/{id}")
    public ResponseEntity<Article> getArticleById(@PathVariable Long id) {
        return articleService.getArticleById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestParam("status") ArticleStatus status) {
        Article updatedArticle = articleService.updateArticleStatus(id, status);
        if (updatedArticle != null) {
            return ResponseEntity.ok(updatedArticle);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Article not found");
        }
    }
}
