package com.example.KDBS.repository;

import com.example.KDBS.enums.ArticleStatus;
import com.example.KDBS.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    List<Article> findByArticleStatus(ArticleStatus status);

    @Query("SELECT a.articleLink FROM Article a WHERE a.articleLink IN :links")
    List<String> findExistingArticleLinks(List<String> links);

    @Query("""
    SELECT COUNT(a)
    FROM Article a
    WHERE a.articleStatus = :status
""")
    long countArticlesByStatus(ArticleStatus status);

}
