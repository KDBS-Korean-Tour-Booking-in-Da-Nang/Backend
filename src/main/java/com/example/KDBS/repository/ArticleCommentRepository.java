package com.example.KDBS.repository;

import com.example.KDBS.model.ArticleComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleCommentRepository extends JpaRepository<ArticleComment, Long> {

    List<ArticleComment> findByArticle_ArticleId(Long articleId);

    List<ArticleComment> findByParentComment_ArticleCommentIdOrderByCreatedAtAsc(Long parentCommentId);
}
