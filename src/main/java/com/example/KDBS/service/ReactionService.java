package com.example.KDBS.service;

import com.example.KDBS.dto.request.ReactionRequest;
import com.example.KDBS.dto.response.ReactionResponse;
import com.example.KDBS.dto.response.ReactionSummaryResponse;
import com.example.KDBS.enums.ReactionTargetType;
import com.example.KDBS.enums.ReactionType;
import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.example.KDBS.mapper.ReactionMapper;
import com.example.KDBS.model.ForumComment;
import com.example.KDBS.model.ForumPost;
import com.example.KDBS.model.Reaction;
import com.example.KDBS.model.User;
import com.example.KDBS.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReactionService {

    private final ReactionRepository reactionRepository;
    private final ForumPostRepository forumPostRepository;
    private final ForumCommentRepository forumCommentRepository;
    private final UserRepository userRepository;
    private final PostImgRepository postImgRepository;
    private final ReactionMapper reactionMapper;   // <-- injected mapper

    @Transactional
    public ReactionResponse addOrUpdateReaction(ReactionRequest request) {
        User user = userRepository.findByEmail(request.getUserEmail())
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_EXISTED));

        // target existence check
        validateTargetExists(request.getTargetId(), request.getTargetType());

        Optional<Reaction> existingReaction = reactionRepository
                .findByUserAndTargetIdAndTargetType(user, request.getTargetId(), request.getTargetType());

        if (existingReaction.isPresent()) {
            Reaction reaction = existingReaction.get();

            // Same type → remove
            if (reaction.getReactionType() == request.getReactionType()) {
                reactionRepository.delete(reaction);
                updateTargetReactionCount(request.getTargetId(), request.getTargetType(), -1);
                return null; // indicates removal
            }

            // Different type → update
            reaction.setReactionType(request.getReactionType());
            Reaction saved = reactionRepository.save(reaction);
            return reactionMapper.toReactionResponse(saved);
        }

        // New reaction
        Reaction newReaction = Reaction.builder()
                .user(user)
                .targetId(request.getTargetId())
                .targetType(request.getTargetType())
                .reactionType(request.getReactionType())
                .build();

        Reaction saved = reactionRepository.save(newReaction);
        updateTargetReactionCount(request.getTargetId(), request.getTargetType(), 1);
        
        return reactionMapper.toReactionResponse(saved);
    }

    @Transactional
    public void removeReaction(ReactionRequest request) {
        User user = userRepository.findByEmail(request.getUserEmail())
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_EXISTED));

        reactionRepository.deleteByUserAndTargetIdAndTargetType(
                user, request.getTargetId(), request.getTargetType());
        updateTargetReactionCount(request.getTargetId(), request.getTargetType(), -1);
    }

    public ReactionSummaryResponse getReactionSummary(Long targetId,
                                                      ReactionTargetType targetType,
                                                      String userEmail) {

        validateTargetExists(targetId, targetType);

        // Count per reaction type
        List<Object[]> rawCounts = reactionRepository.countReactionsByType(targetId, targetType);
        Map<ReactionType, Long> counts = new HashMap<>();
        for (Object[] row : rawCounts) {
            counts.put((ReactionType) row[0], (Long) row[1]);
        }

        Long likeCount    = counts.getOrDefault(ReactionType.LIKE, 0L);
        Long dislikeCount = counts.getOrDefault(ReactionType.DISLIKE, 0L);
        Long total        = likeCount + dislikeCount;

        // User's own reaction (if any)
        ReactionType userReaction = null;
        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail).orElse(null);
            if (user != null) {
                userReaction = reactionRepository
                        .findByUserAndTargetIdAndTargetType(user, targetId, targetType)
                        .map(Reaction::getReactionType)
                        .orElse(null);
            }
        }

        return ReactionSummaryResponse.builder()
                .targetId(targetId)
                .targetType(targetType)
                .likeCount(likeCount)
                .dislikeCount(dislikeCount)
                .totalReactions(total)
                .userReaction(userReaction)
                .build();
    }

    private void validateTargetExists(Long targetId, ReactionTargetType targetType) {
        boolean exists = switch (targetType) {
            case POST    -> forumPostRepository.existsById(targetId);
            case COMMENT -> forumCommentRepository.existsById(targetId);
            case IMG     -> postImgRepository.existsById(targetId);
        };
        if (!exists) {
            throw switch (targetType) {
                case POST    -> new AppException(ErrorCode.POST_NOT_FOUND);
                case COMMENT -> new AppException(ErrorCode.COMMENT_NOT_FOUND);
                case IMG     -> new AppException(ErrorCode.IMAGE_NOT_FOUND);
            };
        }
    }

    private void updateTargetReactionCount(Long targetId,
                                           ReactionTargetType targetType,
                                           int change) {
        switch (targetType) {
            case POST -> {
                ForumPost post = forumPostRepository.findById(targetId).orElse(null);
                if (post != null) {
                    int cur = Optional.ofNullable(post.getReact()).orElse(0);
                    post.setReact(cur + change);
                    forumPostRepository.save(post);
                }
            }
            case COMMENT -> {
                ForumComment comment = forumCommentRepository.findById(targetId).orElse(null);
                if (comment != null) {
                    int cur = Optional.ofNullable(comment.getReact()).orElse(0);
                    comment.setReact(cur + change);
                    forumCommentRepository.save(comment);
                }
            }
            // IMG does not have a reaction count column → nothing to update
        }
    }

    public Long getReactionCountByPost(Long postId) {
        return reactionRepository.countByTargetIdAndTargetType(postId, ReactionTargetType.POST);
    }

    public Boolean hasUserReacted(Long postId, String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) return false;

        return reactionRepository.existsByUserAndTargetIdAndTargetType(
                user, postId, ReactionTargetType.POST);
    }

    public List<ReactionResponse> getUserReactions(String userEmail, String reactionType) {
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) return Collections.emptyList();

        List<Reaction> reactions = reactionRepository.findByUserOrderByCreatedAtDesc(user);

        if (reactionType != null && !reactionType.isEmpty()) {
            reactions = reactions.stream()
                    .filter(r -> r.getReactionType().name().equalsIgnoreCase(reactionType))
                    .toList();
        }

        return reactions.stream()
                .map(reactionMapper::toReactionResponse)
                .collect(Collectors.toList());
    }

    // Backward-compatibility method
    public List<ReactionResponse> getUserReactionsByTargetType(String userEmail,
                                                               ReactionTargetType targetType) {
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) return Collections.emptyList();

        List<Reaction> reactions = targetType != null
                ? reactionRepository.findByUserAndTargetTypeOrderByCreatedAtDesc(user, targetType)
                : reactionRepository.findByUserOrderByCreatedAtDesc(user);

        return reactions.stream()
                .map(reactionMapper::toReactionResponse)
                .collect(Collectors.toList());
    }
}