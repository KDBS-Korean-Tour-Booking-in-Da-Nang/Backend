package com.example.KDBS.service;

import com.example.KDBS.dto.request.ForumCommentRequest;
import com.example.KDBS.dto.response.ForumCommentResponse;
import com.example.KDBS.enums.Role;
import com.example.KDBS.enums.UserActionTarget;
import com.example.KDBS.enums.UserActionType;
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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForumCommentService {

        private final ForumCommentRepository forumCommentRepository;
        private final UserRepository userRepository;
        private final ForumPostRepository forumPostRepository;
        private final CommentMapper commentMapper;
        private final UserActionLogService userActionLogService;

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
                ForumComment parent;
                if (forumCommentRequest.getParentCommentId() != null) {
                        parent = forumCommentRepository.findById(forumCommentRequest.getParentCommentId())
                                        .orElseThrow(() -> new AppException(
                                                        ErrorCode.PARENT_COMMENT_NOT_FOUND));
                        comment.setParentComment(parent);
                }

                comment = forumCommentRepository.save(comment);

                userActionLogService.logAction(
                                user,
                                UserActionType.CREATE_COMMENT,
                                UserActionTarget.COMMENT,
                                comment.getForumCommentId(),
                                Map.of(
                                                "postId", forumPost.getForumPostId(),
                                                "content", forumCommentRequest.getContent(),
                                                "isReply", forumCommentRequest.getParentCommentId() != null));

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

                // Handle child comments before deletion
                // Find all replies to this comment
                List<ForumComment> childComments = forumCommentRepository
                                .findByParentComment_ForumCommentIdOrderByCreatedAtAsc(commentId);

                // Get the parent of the comment being deleted (could be null if it's a root
                // comment)
                ForumComment newParent = comment.getParentComment();

                // Reassign all child comments to point to the deleted comment's parent
                for (ForumComment child : childComments) {
                        child.setParentComment(newParent);
                        forumCommentRepository.save(child);
                        log.debug("Reassigned comment {} parent from {} to {}",
                                        child.getForumCommentId(), commentId,
                                        newParent != null ? newParent.getForumCommentId() : "null");
                }

                // Now we can safely delete the comment
                forumCommentRepository.delete(comment);
                log.info("Comment {} deleted successfully by user {}", commentId, userEmail);
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
}