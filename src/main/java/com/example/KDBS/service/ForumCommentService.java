package com.example.KDBS.service;

import com.example.KDBS.dto.request.ForumCommentRequest;
import com.example.KDBS.dto.response.ForumCommentResponse;
import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
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
public class ForumCommentService {

        @Autowired
        private ForumCommentRepository forumCommentRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private ForumPostRepository forumPostRepository;

        @Autowired
        private CommentMapper commentMapper;

        @Transactional
        public ForumCommentResponse createComment(ForumCommentRequest forumCommentRequest) {
                User user = userRepository.findByEmail(forumCommentRequest.getUserEmail())
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

                ForumPost forumPost = forumPostRepository.findById(forumCommentRequest.getForumPostId())
                        .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

                ForumComment comment = ForumComment.builder()
                        .content(forumCommentRequest.getContent())
                        .imgPath(forumCommentRequest.getImgPath())
                        .user(user)
                        .forumPost(forumPost)
                        .react(0)
                        .build();

                // If this is a reply, set parent comment
                if (forumCommentRequest.getParentCommentId() != null) {
                        ForumComment parent = forumCommentRepository.findById(forumCommentRequest.getParentCommentId())
                                .orElseThrow(() -> new AppException(
                                        ErrorCode.PARENT_COMMENT_NOT_FOUND, forumCommentRequest.getParentCommentId()));
                        ;
                        comment.setParentComment(parent);
                }

                comment = forumCommentRepository.save(comment);
                return commentMapper.toCommentResponse(comment);
        }

        @Transactional(readOnly = true)
        public List<ForumCommentResponse> getCommentsByPostId(Long postId) {
                List<ForumComment> comments = forumCommentRepository.findByForumPost_ForumPostId(postId);
                return comments.stream()
                        .map(commentMapper::toCommentResponse)
                        .collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
        public List<ForumCommentResponse> getReplies(Long parentCommentId) {
                List<ForumComment> replies = forumCommentRepository
                        .findByParentComment_ForumCommentIdOrderByCreatedAtAsc(parentCommentId);
                return replies.stream().map(commentMapper::toCommentResponse).collect(Collectors.toList());
        }

        @Transactional
        public void deleteComment(Long commentId) {
                ForumComment comment = forumCommentRepository.findById(commentId)
                        .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND, commentId));

                // Xóa comment trực tiếp, quyền đã được kiểm tra ở Controller qua @PreAuthorize
                forumCommentRepository.delete(comment);
        }

        @Transactional
        public ForumCommentResponse updateComment(Long id, ForumCommentRequest updateRequest) {
                ForumComment existing = forumCommentRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND, id));

                User user = userRepository.findByEmail(updateRequest.getUserEmail())
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

                // Chỉ cho phép chủ sở hữu update
                if (existing.getUser().getUserId() != user.getUserId()) {
                        throw new AppException(ErrorCode.COMMENT_OWNER_CAN_ONLY_UPDATE_THEIR_OWN_COMMENTS);
                }

                if (updateRequest.getImgPath() != null) {
                        existing.setImgPath(updateRequest.getImgPath());
                }

                if (updateRequest.getForumPostId() != null) {
                        ForumPost forumPost = forumPostRepository.findById(updateRequest.getForumPostId())
                                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND, updateRequest.getForumPostId()));
                        existing.setForumPost(forumPost);
                }

                ForumComment saved = forumCommentRepository.save(existing);
                return commentMapper.toCommentResponse(saved);
        }

        @Transactional(readOnly = true)
        public ForumCommentResponse getCommentById(Long commentId) {
                ForumComment comment = forumCommentRepository.findById(commentId)
                        .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND, commentId));
                return commentMapper.toCommentResponse(comment);
        }
}