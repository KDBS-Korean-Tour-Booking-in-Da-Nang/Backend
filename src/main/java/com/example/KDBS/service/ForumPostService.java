package com.example.KDBS.service;

<<<<<<<< HEAD:src/main/java/com/example/KDBS/service/PostService.java
import com.example.KDBS.dto.request.PostRequest;
import com.example.KDBS.dto.response.PostResponse;
import com.example.KDBS.dto.response.ReactionSummaryResponse;
import com.example.KDBS.enums.ReactionTargetType;
========
import com.example.KDBS.dto.request.ForumPostRequest;
import com.example.KDBS.dto.response.ForumPostResponse;
import com.example.KDBS.dto.response.ReactionSummaryResponse;
import com.example.KDBS.enums.ReactionTargetType;
import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
>>>>>>>> main:src/main/java/com/example/KDBS/service/ForumPostService.java
import com.example.KDBS.mapper.PostMapper;
import com.example.KDBS.model.*;
import com.example.KDBS.repository.*;
import com.example.KDBS.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ForumPostService {

    @Autowired
    private ForumPostRepository forumPostRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostImgRepository postImgRepository;

    @Autowired
    private ForumHashtagRepository forumHashtagRepository;

    @Autowired
    private PostHashtagRepository postHashtagRepository;

    @Autowired
    private SavedPostRepository savedPostRepository;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private ReactionService reactionService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Transactional
<<<<<<<< HEAD:src/main/java/com/example/KDBS/service/PostService.java
    public PostResponse createPost(PostRequest postRequest) throws IOException {
        User user = userRepository.findByEmail(postRequest.getUserEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + postRequest.getUserEmail()));
        ForumPost forumPost = postMapper.toEntity(postRequest);
========
    public ForumPostResponse createPost(ForumPostRequest forumPostRequest) throws IOException {
        User user = userRepository.findByEmail(forumPostRequest.getUserEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        ForumPost forumPost = postMapper.toForumPost(forumPostRequest);
>>>>>>>> main:src/main/java/com/example/KDBS/service/ForumPostService.java
        forumPost.setUser(user);
        forumPost = forumPostRepository.save(forumPost);

        handleImages(forumPostRequest.getImages(), forumPost);
        handleHashtags(forumPostRequest.getHashtags(), forumPost);

        return postMapper.toPostResponse(forumPostRepository.findById(forumPost.getForumPostId())
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND_AFTER_SAVING)));
    }

    @Transactional
    public ForumPostResponse updatePost(Long id, ForumPostRequest updateRequest) throws IOException {
        ForumPost forumPost = forumPostRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND, id));

        User user = userRepository.findByEmail(updateRequest.getUserEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (forumPost.getUser().getUserId() != (user.getUserId())) {
            throw new AppException(ErrorCode.POST_OWNER_CAN_ONLY_UPDATE_THEIR_OWN_POSTS);
        }

        ForumPost updatedPost = postMapper.toForumPost(updateRequest);
        forumPost.setContent(updatedPost.getContent());
        forumPost.setTitle(updatedPost.getTitle());

        postImgRepository.deleteAll(forumPost.getImages());
        postHashtagRepository.deleteAll(forumPost.getHashtags());

        handleImages(updateRequest.getImages(), forumPost);
        handleHashtags(updateRequest.getHashtags(), forumPost);

        forumPost = forumPostRepository.save(forumPost);
        return postMapper.toPostResponse(forumPost);
    }

    @Transactional
    public void deletePost(Long id) {
        // Xóa các saved posts liên quan trước
        deleteRelatedSavedPosts(id);

<<<<<<<< HEAD:src/main/java/com/example/KDBS/service/PostService.java
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));
        if (forumPost.getUser().getUserId() != (user.getUserId())) {
            throw new RuntimeException("User not authorized to delete this post");
        }

        // Delete all related saved posts first to avoid foreign key constraint issues
        // This allows users to delete their posts even if they have been saved by
        // others
        deleteRelatedSavedPosts(id);

        forumPostRepository.delete(forumPost);
========
        // Xóa bài
        forumPostRepository.deleteById(id);
>>>>>>>> main:src/main/java/com/example/KDBS/service/ForumPostService.java
    }

    @Transactional(readOnly = true)
    public List<ForumPostResponse> getAllPosts() {
        return forumPostRepository.findAll().stream()
                .map(postMapper::toPostResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ForumPostResponse getPostById(Long id) {
        ForumPost forumPost = forumPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
        return postMapper.toPostResponse(forumPost);
    }

    private void handleImages(List<MultipartFile> images, ForumPost forumPost) throws IOException {
        if (images != null && !images.isEmpty()) {
            List<PostImg> postImgs = new ArrayList<>();

            for (MultipartFile image : images) {
                String filePath = FileUtils.convertFileToPath(image, uploadDir, "/posts/images");

                PostImg postImg = PostImg.builder()
                        .imgPath(filePath)
                        .forumPost(forumPost)
                        .build();

                postImgs.add(postImg);
            }

            // attach images to post
            forumPost.getImages().clear();
            forumPost.getImages().addAll(postImgs);

            // persist them
            postImgRepository.saveAll(postImgs);
        }
    }

    private void handleHashtags(List<String> hashtags, ForumPost forumPost) {
        if (hashtags != null && !hashtags.isEmpty()) {
            // clear old hashtags
            forumPost.getHashtags().clear();

            // Normalize and deduplicate hashtags
            List<String> normalizedTags = hashtags.stream()
                    .map(String::toLowerCase)
                    .distinct()
                    .collect(Collectors.toList());

            // Batch fetch existing hashtags
            List<ForumHashtag> existingHashtags = forumHashtagRepository.findByContentIn(normalizedTags);
            Map<String, ForumHashtag> hashtagMap = existingHashtags.stream()
                    .collect(Collectors.toMap(h -> h.getContent().toLowerCase(), h -> h));

            for (String tagContent : normalizedTags) {
                ForumHashtag hashtag = hashtagMap.computeIfAbsent(tagContent, k -> {
                    ForumHashtag newHashtag = ForumHashtag.builder()
                            .content(tagContent)
                            .build();
                    return forumHashtagRepository.save(newHashtag);
                });

                PostHashtag postHashtag = PostHashtag.builder()
                        .forumPost(forumPost)
                        .hashtag(hashtag)
                        .build();

                forumPost.getHashtags().add(postHashtag);
                postHashtagRepository.save(postHashtag);
            }
        }
    }

    @Transactional(readOnly = true)
<<<<<<<< HEAD:src/main/java/com/example/KDBS/service/PostService.java
    public Page<PostResponse> searchPosts(String keyword, List<String> hashtags, Pageable pageable) {
========
    public Page<ForumPostResponse> searchPosts(String keyword, List<String> hashtags, Pageable pageable) {
>>>>>>>> main:src/main/java/com/example/KDBS/service/ForumPostService.java
        List<String> normalizedTags = null;
        if (hashtags != null && !hashtags.isEmpty()) {
            // Normalize hashtag to lowercase
            normalizedTags = hashtags.stream().map(String::toLowerCase).toList();
        }

        return forumPostRepository.searchPosts(keyword, normalizedTags, pageable)
<<<<<<<< HEAD:src/main/java/com/example/KDBS/service/PostService.java
                .map(postMapper::toResponse);
========
                .map(postMapper::toPostResponse);
>>>>>>>> main:src/main/java/com/example/KDBS/service/ForumPostService.java
    }

    /**
     * Delete all saved posts related to a specific post
     * This allows post authors to delete their posts even if they have been saved
     * by others
     */
    private void deleteRelatedSavedPosts(Long postId) {
        try {
            // Find all saved posts for this post
            List<SavedPost> savedPosts = savedPostRepository.findByPostIdOrderBySavedAtDesc(postId);

            if (!savedPosts.isEmpty()) {
                // Delete all saved posts
                savedPostRepository.deleteAll(savedPosts);
                System.out.println("Deleted " + savedPosts.size() + " saved posts for post ID: " + postId);
            }
        } catch (Exception e) {
            System.err.println("Error deleting related saved posts for post ID " + postId + ": " + e.getMessage());
            // Don't throw exception here to allow post deletion to continue
        }
    }

    @Transactional(readOnly = true)
<<<<<<<< HEAD:src/main/java/com/example/KDBS/service/PostService.java
    public Page<PostResponse> getPostsByUser(String userEmail, Pageable pageable) {
========
    public Page<ForumPostResponse> getPostsByUser(String userEmail, Pageable pageable) {
>>>>>>>> main:src/main/java/com/example/KDBS/service/ForumPostService.java
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        Page<ForumPost> posts = forumPostRepository.findByUserOrderByCreatedAtDesc(user, pageable);

        return posts.map(post -> {
<<<<<<<< HEAD:src/main/java/com/example/KDBS/service/PostService.java
            PostResponse response = postMapper.toResponse(post);
========
            ForumPostResponse response = postMapper.toPostResponse(post);
>>>>>>>> main:src/main/java/com/example/KDBS/service/ForumPostService.java

            // Get reaction summary
            ReactionSummaryResponse reactionSummary = reactionService.getReactionSummary(
                    post.getForumPostId(),
                    ReactionTargetType.POST,
                    userEmail);

            // Get save count
            Long saveCount = savedPostRepository.countByPostId(post.getForumPostId());

            // Set reaction summary and save count
            response.setReactions(reactionSummary);
            response.setSaveCount(saveCount);

            return response;
        });
    }

}