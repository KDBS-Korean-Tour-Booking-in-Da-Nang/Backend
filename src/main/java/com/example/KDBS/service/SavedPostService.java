package com.example.KDBS.service;

import com.example.KDBS.dto.request.SavePostRequest;
import com.example.KDBS.dto.response.SavedPostResponse;
import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.example.KDBS.model.ForumPost;
import com.example.KDBS.model.SavedPost;
import com.example.KDBS.model.User;
import com.example.KDBS.repository.ForumPostRepository;
import com.example.KDBS.repository.SavedPostRepository;
import com.example.KDBS.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavedPostService {

    private final SavedPostRepository savedPostRepository;
    private final ForumPostRepository forumPostRepository;
    private final UserRepository userRepository;

    @Transactional
    public SavedPostResponse savePost(SavePostRequest request, String userEmail) {
        // tim user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // tim post
        ForumPost post = forumPostRepository.findById(request.getPostId())
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        // check post cua ban than
        if (post.getUser().getUserId() == user.getUserId()) {
            throw new AppException(ErrorCode.CANNOT_SAVE_OWN_POST);
        }

        // check user da save post chua
        if (savedPostRepository.findByUserAndPost(user, post).isPresent()) {
            throw new AppException(ErrorCode.POST_ALREADY_SAVED);
        }

        // Tạo saved post
        SavedPost savedPost = SavedPost.builder()
                .user(user)
                .post(post)
                .note(request.getNote())
                .build();

        SavedPost saved = savedPostRepository.save(savedPost);
        return mapToResponse(saved);
    }

    @Transactional
    public void unsavePost(Long postId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        SavedPost savedPost = savedPostRepository.findByUserAndPost(user, post)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_SAVED));

        savedPostRepository.delete(savedPost);
    }

    @Transactional(readOnly = true)
    public List<SavedPostResponse> getSavedPostsByUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        List<SavedPost> savedPosts = savedPostRepository.findByUserIdOrderBySavedAtDesc((long) user.getUserId());
        
        return savedPosts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean isPostSavedByUser(Long postId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return savedPostRepository.findByUserIdAndPostId((long) user.getUserId(), postId).isPresent();
    }

    @Transactional(readOnly = true)
    public Long getSaveCountByPost(Long postId) {
        return savedPostRepository.countByPostId(postId);
    }

    @Transactional(readOnly = true)
    public Long getSaveCountByUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return savedPostRepository.countByUserId((long) user.getUserId());
    }

    @Transactional(readOnly = true)
    public List<String> getUsersWhoSavedPost(Long postId) {
        List<SavedPost> savedPosts = savedPostRepository.findByPostIdOrderBySavedAtDesc(postId);
        
        return savedPosts.stream()
                .map(sp -> sp.getUser().getUsername())
                .collect(Collectors.toList());
    }

    private SavedPostResponse mapToResponse(SavedPost savedPost) {
        ForumPost post = savedPost.getPost();
        User postAuthor = post.getUser();

        return SavedPostResponse.builder()
                .savedPostId(savedPost.getSavedPostId())
                .postId(post.getForumPostId())
                .postTitle(post.getTitle())
                .postContent(post.getContent())
                .postAuthor(postAuthor.getUsername())
                .postAuthorAvatar(postAuthor.getAvatar())
                .postCreatedAt(post.getCreatedAt())
                .note(savedPost.getNote())
                .savedAt(savedPost.getSavedAt())
                .build();
    }
}
