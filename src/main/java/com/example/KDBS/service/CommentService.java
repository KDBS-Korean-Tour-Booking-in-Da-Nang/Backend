package com.example.KDBS.service;

import com.example.KDBS.dto.request.CommentRequest;
import com.example.KDBS.dto.response.CommentResponse;
import com.example.KDBS.mapper.CommentMapper;
import com.example.KDBS.model.ForumComment;
import com.example.KDBS.model.ForumPost;
import com.example.KDBS.model.User;
import com.example.KDBS.repository.ForumCommentRepository;
import com.example.KDBS.repository.ForumPostRepository;
import com.example.KDBS.repository.UserRepository;
import com.example.KDBS.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private ForumCommentRepository forumCommentRepository;

    @Autowired
    private ForumPostRepository forumPostRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentMapper commentMapper;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Transactional
    public CommentResponse createComment(CommentRequest commentRequest) throws IOException {
        ForumPost forumPost = forumPostRepository.findById(commentRequest.getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + commentRequest.getPostId()));

        User user = userRepository.findByEmail(commentRequest.getUserEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + commentRequest.getUserEmail()));

        ForumComment forumComment = commentMapper.toEntity(commentRequest);
        forumComment.setUser(user);
        forumComment.setForumPost(forumPost);
        forumComment.setReact(0); // Initialize react count to 0

        handleImage(commentRequest.getImgPath(), forumComment);

        forumComment = forumCommentRepository.save(forumComment);
        return commentMapper.toResponse(forumComment);
    }

    @Transactional
    public CommentResponse updateComment(Long id, CommentRequest updateRequest) throws IOException {
        ForumComment forumComment = forumCommentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + id));

        User user = userRepository.findByEmail(updateRequest.getUserEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + updateRequest.getUserEmail()));
        if (forumComment.getUser().getUserId() != user.getUserId()) {
            throw new RuntimeException("User not authorized to update this comment");
        }

        ForumPost forumPost = forumPostRepository.findById(updateRequest.getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + updateRequest.getPostId()));

        ForumComment updatedComment = commentMapper.toEntity(updateRequest);
        forumComment.setContent(updatedComment.getContent());
        forumComment.setForumPost(forumPost);

        forumComment.setImgPath(null);
        handleImage(updateRequest.getImgPath(), forumComment);

        forumComment = forumCommentRepository.save(forumComment);
        return commentMapper.toResponse(forumComment);
    }

    @Transactional
    public void deleteComment(Long id, String userEmail) {
        ForumComment forumComment = forumCommentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + id));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));
        if (forumComment.getUser().getUserId() != user.getUserId()) {
            throw new RuntimeException("User not authorized to delete this comment");
        }

        forumCommentRepository.delete(forumComment);
    }

        public List<CommentResponse> getCommentsByPostId(Long postId) {
        return forumCommentRepository.findByForumPost_ForumPostId(postId).stream()
                .map(commentMapper::toResponse)
                .collect(Collectors.toList());
    }

    private void handleImage(MultipartFile imgFile, ForumComment forumComment) throws IOException {
        if (imgFile != null) {
            String imgPath = FileUtils.convertFileToPath(imgFile, uploadDir, "/comments/images");
            forumComment.setImgPath(imgPath);
        }
    }
}