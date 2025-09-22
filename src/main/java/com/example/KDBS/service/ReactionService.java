package com.example.KDBS.service;

import com.example.KDBS.dto.request.ReactionRequest;
import com.example.KDBS.dto.response.ReactionResponse;
import com.example.KDBS.dto.response.ReactionSummaryResponse;
import com.example.KDBS.enums.ReactionTargetType;
import com.example.KDBS.enums.ReactionType;
import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.example.KDBS.model.ForumPost;
import com.example.KDBS.model.ForumComment;
import com.example.KDBS.model.Reaction;
import com.example.KDBS.model.User;
import com.example.KDBS.repository.ForumPostRepository;
import com.example.KDBS.repository.ForumCommentRepository;
import com.example.KDBS.repository.ReactionRepository;
import com.example.KDBS.repository.UserRepository;
import com.example.KDBS.repository.PostImgRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReactionService {

    @Autowired
    private ReactionRepository reactionRepository;
    @Autowired
    private ForumPostRepository forumPostRepository;
    @Autowired
    private ForumCommentRepository forumCommentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostImgRepository postImgRepository;

    @Transactional
    public ReactionResponse addOrUpdateReaction(ReactionRequest request, String userEmail) {
        System.out.println("fiding" + userEmail);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("not found" + userEmail));

        System.out.println("found " + user.getUsername() + " (ID: " + user.getUserId() + ")");

        // check target exists
        validateTargetExists(request.getTargetId(), request.getTargetType());

        // check user da react target
        Optional<Reaction> existingReaction = reactionRepository.findByUserAndTargetIdAndTargetType(
                user, request.getTargetId(), request.getTargetType());

        if (existingReaction.isPresent()) {
            Reaction reaction = existingReaction.get();
            // da react ->
            if (reaction.getReactionType() == request.getReactionType()) {
                reactionRepository.delete(reaction);
                updateTargetReactionCount(request.getTargetId(), request.getTargetType(), -1);
                return null; // Reaction removed
            } else {
                // update to new reaction type
                reaction.setReactionType(request.getReactionType());
                Reaction savedReaction = reactionRepository.save(reaction);
                return mapToResponse(savedReaction);
            }
        } else {
            // create new reaction
            Reaction newReaction = Reaction.builder()
                    .user(user)
                    .targetId(request.getTargetId())
                    .targetType(request.getTargetType())
                    .reactionType(request.getReactionType())
                    .build();

            Reaction savedReaction = reactionRepository.save(newReaction);
            updateTargetReactionCount(request.getTargetId(), request.getTargetType(), 1);
            return mapToResponse(savedReaction);
        }
    }

    @Transactional
    public void removeReaction(Long targetId, ReactionTargetType targetType, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        reactionRepository.deleteByUserAndTargetIdAndTargetType(user, targetId, targetType);
        updateTargetReactionCount(targetId, targetType, -1);
    }

    public ReactionSummaryResponse getReactionSummary(Long targetId, ReactionTargetType targetType, String userEmail) {
        // check target exists
        validateTargetExists(targetId, targetType);

        // Get reaction counts by type
        List<Object[]> reactionCounts = reactionRepository.countReactionsByType(targetId, targetType);
        Map<ReactionType, Long> reactionMap = new HashMap<>();

        for (Object[] result : reactionCounts) {
            ReactionType type = (ReactionType) result[0];
            Long count = (Long) result[1];
            reactionMap.put(type, count);
        }

        Long likeCount = reactionMap.getOrDefault(ReactionType.LIKE, 0L);
        Long dislikeCount = reactionMap.getOrDefault(ReactionType.DISLIKE, 0L);
        Long totalReactions = likeCount + dislikeCount;

        // Get user's reaction if logged in
        ReactionType userReaction = null;
        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail).orElse(null);
            if (user != null) {
                Optional<Reaction> userReactionOpt = reactionRepository.findByUserAndTargetIdAndTargetType(
                        user, targetId, targetType);
                if (userReactionOpt.isPresent()) {
                    userReaction = userReactionOpt.get().getReactionType();
                }
            }
        }

        return ReactionSummaryResponse.builder()
                .targetId(targetId)
                .targetType(targetType)
                .likeCount(likeCount)
                .dislikeCount(dislikeCount)
                .totalReactions(totalReactions)
                .userReaction(userReaction)
                .build();
    }

    public List<ReactionResponse> getReactionsByTarget(Long targetId, ReactionTargetType targetType) {
        List<Reaction> reactions = reactionRepository.findByTargetIdAndTargetType(targetId, targetType);
        return reactions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void validateTargetExists(Long targetId, ReactionTargetType targetType) {
        if (targetType == ReactionTargetType.POST) {
            if (!forumPostRepository.existsById(targetId)) {
                throw new AppException(ErrorCode.POST_NOT_FOUND);
            }
        } else if (targetType == ReactionTargetType.COMMENT) {
            if (!forumCommentRepository.existsById(targetId)) {
                throw new AppException(ErrorCode.COMMENT_NOT_FOUND);
            }
        } else if (targetType == ReactionTargetType.IMG) {
            if (!postImgRepository.existsById(targetId)) {
                throw new AppException(ErrorCode.IMAGE_NOT_FOUND);
            }
        }
    }

    private void updateTargetReactionCount(Long targetId, ReactionTargetType targetType, int change) {
        if (targetType == ReactionTargetType.POST) {
            ForumPost post = forumPostRepository.findById(targetId).orElse(null);
            if (post != null) {
                Integer currentReact = post.getReact() != null ? post.getReact() : 0;
                post.setReact(currentReact + change);
                forumPostRepository.save(post);
            }
        } else if (targetType == ReactionTargetType.COMMENT) {
            ForumComment comment = forumCommentRepository.findById(targetId).orElse(null);
            if (comment != null) {
                Integer currentReact = comment.getReact() != null ? comment.getReact() : 0;
                comment.setReact(currentReact + change);
                forumCommentRepository.save(comment);
            }
        }
    }

    private ReactionResponse mapToResponse(Reaction reaction) {
        return ReactionResponse.builder()
                .reactionId(reaction.getReactionId())
                .reactionType(reaction.getReactionType())
                .targetType(reaction.getTargetType())
                .targetId(reaction.getTargetId())
                .username(reaction.getUser().getUsername())
                .userAvatar(reaction.getUser().getAvatar())
                .createdAt(reaction.getCreatedAt())
                .build();
    }

    // New methods for simplified reaction handling
    @Transactional
    public ReactionResponse createReaction(ReactionRequest reactionRequest) {
        return addOrUpdateReaction(reactionRequest, reactionRequest.getUserEmail());
    }

    @Transactional
    public void removeReactionByRequest(ReactionRequest reactionRequest) {
        removeReaction(reactionRequest.getTargetId(), reactionRequest.getTargetType(), reactionRequest.getUserEmail());
    }

    public Long getReactionCountByPost(Long postId) {
        return reactionRepository.countByTargetIdAndTargetType(postId, ReactionTargetType.POST);
    }

    public Boolean hasUserReacted(Long postId, String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null)
            return false;

        return reactionRepository.existsByUserAndTargetIdAndTargetType(
                user, postId, ReactionTargetType.POST);
    }

    public List<ReactionResponse> getUserReactions(String userEmail, String reactionType) {
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            return new ArrayList<>();
        }

        List<Reaction> reactions;
        if (reactionType != null && !reactionType.isEmpty()) {
            // Filter by reaction type (LIKE, DISLIKE)
            reactions = reactionRepository.findByUserOrderByCreatedAtDesc(user);
            reactions = reactions.stream()
                    .filter(r -> r.getReactionType().name().equals(reactionType))
                    .collect(Collectors.toList());
        } else {
            reactions = reactionRepository.findByUserOrderByCreatedAtDesc(user);
        }

        return reactions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Keep original method for backward compatibility
    public List<ReactionResponse> getUserReactionsByTargetType(String userEmail, ReactionTargetType targetType) {
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            return new ArrayList<>();
        }

        List<Reaction> reactions;
        if (targetType != null) {
            reactions = reactionRepository.findByUserAndTargetTypeOrderByCreatedAtDesc(user, targetType);
        } else {
            reactions = reactionRepository.findByUserOrderByCreatedAtDesc(user);
        }

        return reactions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}
