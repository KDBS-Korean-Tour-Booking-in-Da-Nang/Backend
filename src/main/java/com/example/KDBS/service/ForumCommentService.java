package com.example.KDBS.service;

import com.example.KDBS.dto.request.ForumCommentRequest;
import com.example.KDBS.dto.response.ForumCommentResponse;
import com.example.KDBS.enums.NotificationType;
import com.example.KDBS.enums.Role;
import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.example.KDBS.mapper.CommentMapper;
import com.example.KDBS.model.ForumComment;
import com.example.KDBS.model.ForumPost;
import com.example.KDBS.model.User;
import com.example.KDBS.repository.ForumCommentRepository;
import com.example.KDBS.repository.ForumPostRepository;
import com.example.KDBS.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForumCommentService {

        private final ForumCommentRepository forumCommentRepository;
        private final UserRepository userRepository;
        private final ForumPostRepository forumPostRepository;
        private final CommentMapper commentMapper;
        private final NotificationService notificationService;

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
                ForumComment parent = null;
                if (forumCommentRequest.getParentCommentId() != null) {
                        parent = forumCommentRepository.findById(forumCommentRequest.getParentCommentId())
                                .orElseThrow(() -> new AppException(
                                        ErrorCode.PARENT_COMMENT_NOT_FOUND));
                        comment.setParentComment(parent);
                }

                comment = forumCommentRepository.save(comment);
                
                // Tạo thông báo sau khi lưu comment thành công
                createNotificationForComment(comment, parent);
                
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
    public void deleteComment(Long commentId, String userEmail) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        ForumComment comment = forumCommentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        User owner = comment.getUser();
        Role currentRole = currentUser.getRole();
        Role ownerRole = owner.getRole();

        // Authorization checks
        if (currentRole == Role.ADMIN) {
            log.debug("Admin {} is deleting comment {}", userEmail, commentId);
        } else if (currentRole == Role.STAFF) {
            if (ownerRole == Role.ADMIN) {
                throw new AppException(ErrorCode.STAFF_CANNOT_DELETE_ADMIN_COMMENTS);
            }
        } else if (currentRole == Role.COMPANY || currentRole == Role.USER) {
            if (!owner.getEmail().equals(userEmail)) {
                throw new AppException(ErrorCode.USER_OR_COMPANY_CAN_ONLY_DELETE_THEIR_OWN_COMMENTS);
            }
        } else {
            throw new AppException(ErrorCode.YOU_DO_NOT_HAVE_PERMISSION_TO_DELETE_THIS_COMMENT);
        }

        // Delete the comment
        forumCommentRepository.delete(comment);
    }

        @Transactional
        public ForumCommentResponse updateComment(Long id, ForumCommentRequest updateRequest) {
                ForumComment existing = forumCommentRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

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
                                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
                        existing.setForumPost(forumPost);
                }

                ForumComment saved = forumCommentRepository.save(existing);
                return commentMapper.toCommentResponse(saved);
        }

        @Transactional(readOnly = true)
        public ForumCommentResponse getCommentById(Long commentId) {
                ForumComment comment = forumCommentRepository.findById(commentId)
                        .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));
                return commentMapper.toCommentResponse(comment);
        }

        private void createNotificationForComment(ForumComment comment, ForumComment parentComment) {
                try {
                        User actor = comment.getUser();
                        ForumPost post = comment.getForumPost();
                        User postOwner = post.getUser();

                        // reply comment
                        if (parentComment != null) {
                                User commentOwner = parentComment.getUser();
                                
                                // noti cho owner cua comment
                                if (commentOwner.getUserId() != actor.getUserId()) {
                                        String commentPreview = parentComment.getContent() != null 
                                                && parentComment.getContent().length() > 50
                                                ? parentComment.getContent().substring(0, 50) + "..."
                                                : parentComment.getContent();
                                        
                                        notificationService.createNotification(
                                                commentOwner.getUserId(),
                                                actor.getUserId(),
                                                NotificationType.REPLY_COMMENT,
                                                parentComment.getForumCommentId(),
                                                "COMMENT",
                                                "Ai đó đã trả lời bình luận của bạn",
                                                String.format("%s đã trả lời bình luận của bạn: \"%s\"",
                                                        actor.getUsername() != null ? actor.getUsername() : "Người dùng",
                                                        commentPreview != null ? commentPreview : "")
                                        );
                                }
                        }

                        // noti for new comment
                        // Không thông báo nếu đã thông báo reply ở trên và cùng một người
                        if (postOwner.getUserId() != actor.getUserId() 
                                && (parentComment == null || parentComment.getUser().getUserId() != postOwner.getUserId())) {
                                notificationService.createNotification(
                                        postOwner.getUserId(),
                                        actor.getUserId(),
                                        NotificationType.COMMENT_POST,
                                        post.getForumPostId(),
                                        "POST",
                                        "Ai đó đã bình luận bài viết của bạn",
                                        String.format("%s đã bình luận bài viết \"%s\" của bạn",
                                                actor.getUsername() != null ? actor.getUsername() : "Người dùng",
                                                post.getTitle() != null && !post.getTitle().isEmpty() 
                                                        ? post.getTitle() 
                                                        : "của bạn")
                                );
                        }
                } catch (Exception e) {
                        log.error("Failed to create notification for comment: {}", e.getMessage(), e);
                }
        }
}