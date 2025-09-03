package com.example.KDBS.repository;

import com.example.KDBS.model.ForumComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumCommentRepository extends JpaRepository<ForumComment, Long> {
    List<ForumComment> findByForumPost_ForumPostId(Long forumPostId);
}
