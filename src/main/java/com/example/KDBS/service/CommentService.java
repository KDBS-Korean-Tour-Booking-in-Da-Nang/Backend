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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

        @Autowired
        private ForumCommentRepository forumCommentRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private ForumPostRepository forumPostRepository;

        @Autowired
        private CommentMapper commentMapper;

        @Transactional
        public CommentResponse createComment(CommentRequest commentRequest) {
                User user = userRepository.findByEmail(commentRequest.getUserEmail())
                                .orElseThrow(() -> new RuntimeException(
                                                "User not found with email: " + commentRequest.getUserEmail()));

                ForumPost forumPost = forumPostRepository.findById(commentRequest.getForumPostId())
                                .orElseThrow(() -> new RuntimeException(
                                                "Post not found with id: " + commentRequest.getForumPostId()));

                ForumComment comment = ForumComment.builder()
                                .content(commentRequest.getContent())
                                .imgPath(commentRequest.getImgPath())
                                .user(user)
                                .forumPost(forumPost)
                                .react(0)
                                .build();

                // If this is a reply, set parent comment
                if (commentRequest.getParentCommentId() != null) {
                        ForumComment parent = forumCommentRepository.findById(commentRequest.getParentCommentId())
                                        .orElseThrow(() -> new RuntimeException("Parent comment not found with id: "
                                                        + commentRequest.getParentCommentId()));
                        comment.setParentComment(parent);
                }

                comment = forumCommentRepository.save(comment);
                return commentMapper.toResponse(comment);
        }

        @Transactional(readOnly = true)
        public List<CommentResponse> getCommentsByPostId(Long postId) {
                List<ForumComment> comments = forumCommentRepository.findByForumPost_ForumPostId(postId);
                return comments.stream()
                                .map(commentMapper::toResponse)
                                .collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
        public List<CommentResponse> getReplies(Long parentCommentId) {
                List<ForumComment> replies = forumCommentRepository
                                .findByParentComment_ForumCommentIdOrderByCreatedAtAsc(parentCommentId);
                return replies.stream().map(commentMapper::toResponse).collect(Collectors.toList());
        }

        @Transactional
        public void deleteComment(Long commentId, String userEmail) {
                ForumComment comment = forumCommentRepository.findById(commentId)
                                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

                User user = userRepository.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

                if (comment.getUser().getUserId() != user.getUserId()) {
                        throw new RuntimeException("User not authorized to delete this comment");
                }

                forumCommentRepository.delete(comment);
        }

        @Transactional
        public CommentResponse updateComment(Long id, CommentRequest updateRequest) {
                ForumComment existing = forumCommentRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + id));

                User user = userRepository.findByEmail(updateRequest.getUserEmail())
                                .orElseThrow(() -> new RuntimeException(
                                                "User not found with email: " + updateRequest.getUserEmail()));

                if (existing.getUser().getUserId() != user.getUserId()) {
                        throw new RuntimeException("User not authorized to update this comment");
                }

                if (updateRequest.getContent() != null) {
                        existing.setContent(updateRequest.getContent());
                }

                if (updateRequest.getImgPath() != null) {
                        existing.setImgPath(updateRequest.getImgPath());
                }

                if (updateRequest.getForumPostId() != null) {
                        ForumPost forumPost = forumPostRepository.findById(updateRequest.getForumPostId())
                                        .orElseThrow(() -> new RuntimeException("Post not found with id: "
                                                        + updateRequest.getForumPostId()));
                        existing.setForumPost(forumPost);
                }

                ForumComment saved = forumCommentRepository.save(existing);
                return commentMapper.toResponse(saved);
        }
}