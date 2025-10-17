package com.example.KDBS.repository;

import com.example.KDBS.enums.ArticleStatus;
import com.example.KDBS.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    Optional<Article> findByArticleLink(String articleLink);

    List<Article> findByArticleStatus(ArticleStatus status);
}
