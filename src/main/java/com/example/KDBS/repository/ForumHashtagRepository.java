package com.example.KDBS.repository;

import com.example.KDBS.model.ForumHashtag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ForumHashtagRepository extends JpaRepository<ForumHashtag, Long> {
        Optional<ForumHashtag> findByContent(String content);

        List<ForumHashtag> findByContentIn(List<String> contents);

        @Query("SELECT h.content, COUNT(ph), SUM(p.react) FROM ForumHashtag h " +
                        "JOIN PostHashtag ph ON h.id = ph.hashtag.id " +
                        "JOIN ForumPost p ON ph.forumPost.id = p.forumPostId " +
                        "GROUP BY h.content " +
                        "ORDER BY COUNT(ph) DESC, SUM(p.react) DESC")
        List<Object[]> findPopularHashtags(Pageable pageable);

        @Query("SELECT h.content, COUNT(ph), SUM(p.react) FROM ForumHashtag h " +
                        "JOIN PostHashtag ph ON h.id = ph.hashtag.id " +
                        "JOIN ForumPost p ON ph.forumPost.id = p.forumPostId " +
                        "WHERE h.content LIKE %:keyword% " +
                        "GROUP BY h.content " +
                        "ORDER BY COUNT(ph) DESC, SUM(p.react) DESC")
        List<Object[]> searchHashtags(@Param("keyword") String keyword, Pageable pageable);
}