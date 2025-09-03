package com.example.KDBS.repository;

import com.example.KDBS.model.ForumPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {

    // Tìm theo title hoặc content chứa keyword + lọc theo hashtag
    @Query("SELECT DISTINCT p FROM ForumPost p " +
            "LEFT JOIN p.hashtags ph " +
            "LEFT JOIN ph.hashtag h " +
            "WHERE ((:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "   OR (:keyword IS NULL OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')))) " +
            "AND (:hashtags IS NULL OR LOWER(h.content) IN :hashtags)")
    Page<ForumPost> searchPosts(@Param("keyword") String keyword,
                                @Param("hashtags") List<String> hashtags,
                                Pageable pageable);
}
