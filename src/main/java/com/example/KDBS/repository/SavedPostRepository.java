package com.example.KDBS.repository;

import com.example.KDBS.model.SavedPost;
import com.example.KDBS.model.User;
import com.example.KDBS.model.ForumPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedPostRepository extends JpaRepository<SavedPost, Long> {

    // check user da save post chua
    Optional<SavedPost> findByUserAndPost(User user, ForumPost post);
    
    // check user da save post chua dung ID
    @Query("SELECT sp FROM SavedPost sp WHERE sp.user.userId = :userId AND sp.post.forumPostId = :postId")
    Optional<SavedPost> findByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);
    
    // lay danh sach post da save cua user
    @Query("SELECT sp FROM SavedPost sp WHERE sp.user.userId = :userId ORDER BY sp.savedAt DESC")
    List<SavedPost> findByUserIdOrderBySavedAtDesc(@Param("userId") Long userId);
    
    // lay danh sach user da save post
    @Query("SELECT sp FROM SavedPost sp WHERE sp.post.forumPostId = :postId ORDER BY sp.savedAt DESC")
    List<SavedPost> findByPostIdOrderBySavedAtDesc(@Param("postId") Long postId);
    
    // dem so luong save cua post
    @Query("SELECT COUNT(sp) FROM SavedPost sp WHERE sp.post.forumPostId = :postId")
    Long countByPostId(@Param("postId") Long postId);
    
    // dem so luong post da save cua user
    @Query("SELECT COUNT(sp) FROM SavedPost sp WHERE sp.user.userId = :userId")
    Long countByUserId(@Param("userId") Long userId);
    
    // xoa save post
    void deleteByUserAndPost(User user, ForumPost post);
    
    // xoa save post bang id
    @Modifying
    @Query("DELETE FROM SavedPost sp WHERE sp.user.userId = :userId AND sp.post.forumPostId = :postId")
    void deleteByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);
}
