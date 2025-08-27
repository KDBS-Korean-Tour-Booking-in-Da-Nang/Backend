package com.example.KDBS.repository;

import com.example.KDBS.model.ForumComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ForumCommentRepository extends JpaRepository<ForumComment, Long> {
    List<ForumComment> findByForumPost_ForumPostId(Long forumPostId);
}
