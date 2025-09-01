package com.example.KDBS.service;

import com.example.KDBS.dto.request.PostRequest;
import com.example.KDBS.dto.response.PostResponse;
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
public class PostService {

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
    private PostMapper postMapper;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Transactional
    public PostResponse createPost(PostRequest postRequest) throws IOException {
        User user = userRepository.findByEmail(postRequest.getUserEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + postRequest.getUserEmail()));
        ForumPost forumPost = postMapper.toEntity(postRequest);
        forumPost.setUser(user);
        forumPost = forumPostRepository.save(forumPost);

        handleImages(postRequest.getImageUrls(), forumPost);
        handleHashtags(postRequest.getHashtags(), forumPost);

        return postMapper.toResponse(forumPostRepository.findById(forumPost.getForumPostId())
                .orElseThrow(() -> new RuntimeException("Post not found after saving")));
    }

    @Transactional
    public PostResponse updatePost(Long id, PostRequest updateRequest) throws IOException {
        ForumPost forumPost = forumPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

        User user = userRepository.findByEmail(updateRequest.getUserEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + updateRequest.getUserEmail()));
        if (forumPost.getUser().getUserId() != (user.getUserId())) {
            throw new RuntimeException("User not authorized to update this post");
        }

        ForumPost updatedPost = postMapper.toEntity(updateRequest);
        forumPost.setContent(updatedPost.getContent());
        forumPost.setTitle(updatedPost.getTitle());

        postImgRepository.deleteAll(forumPost.getImages());
        postHashtagRepository.deleteAll(forumPost.getHashtags());

        handleImages(updateRequest.getImageUrls(), forumPost);
        handleHashtags(updateRequest.getHashtags(), forumPost);

        forumPost = forumPostRepository.save(forumPost);
        return postMapper.toResponse(forumPost);
    }

    @Transactional
    public void deletePost(Long id, String userEmail) {
        ForumPost forumPost = forumPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));
        if (forumPost.getUser().getUserId() != (user.getUserId())) {
            throw new RuntimeException("User not authorized to delete this post");
        }

        forumPostRepository.delete(forumPost);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getAllPosts() {
        return forumPostRepository.findAll().stream()
                .map(postMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PostResponse getPostById(Long id) {
        ForumPost forumPost = forumPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
        return postMapper.toResponse(forumPost);
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
    public Page<PostResponse> searchPosts(String keyword, List<String> hashtags, Pageable pageable) {
        List<String> normalizedTags = null;
        if (hashtags != null && !hashtags.isEmpty()) {
            // Normalize hashtag to lowercase
            normalizedTags = hashtags.stream().map(String::toLowerCase).toList();
        }

        return forumPostRepository.searchPosts(keyword, normalizedTags, pageable)
                .map(postMapper::toResponse);
    }

}