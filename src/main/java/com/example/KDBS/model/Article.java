package com.example.KDBS.model;

import com.example.KDBS.enums.ArticleStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "articles")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "article_id")
    private Long articleId;

    @Column(name = "article_thumbnail", length = 500)
    private String articleThumbnail;

    @Column(name = "article_title", nullable = false)
    private String articleTitle;

    @Column(name = "article_description", length = 1000)
    private String articleDescription;

    @Column(name = "article_content", columnDefinition = "LONGTEXT")
    private String articleContent;

    @Column(name = "article_title_en")
    private String articleTitleEN;

    @Column(name = "article_description_en", length = 1000)
    private String articleDescriptionEN;

    @Column(name = "article_content_en", columnDefinition = "LONGTEXT")
    private String articleContentEN;

    @Column(name = "article_title_kr")
    private String articleTitleKR;

    @Column(name = "article_description_kr", length = 1000)
    private String articleDescriptionKR;

    @Column(name = "article_content_kr", columnDefinition = "LONGTEXT")
    private String articleContentKR;

    @Column(name = "article_link", unique = true, length = 500)
    private String articleLink;

    @Column(name = "article_summary", length = 2000)
    private String articleSummary;

    @Enumerated(EnumType.STRING)
    @Column(name = "article_status", length = 20)
    private ArticleStatus articleStatus;

    @Column(name = "article_created_date")
    private LocalDateTime articleCreatedDate;

    @PrePersist
    protected void onCreate() {
        this.articleCreatedDate = LocalDateTime.now();
        this.articleStatus = ArticleStatus.PENDING;
    }
}
