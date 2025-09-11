package com.example.KDBS.dto.response;

import com.example.KDBS.enums.ReactionTargetType;
import com.example.KDBS.enums.ReactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReactionSummaryResponse {
    
    private Long targetId;
    private ReactionTargetType targetType;
    private Long likeCount;
    private Long dislikeCount;
    private Long totalReactions;
    private ReactionType userReaction; // null neu user chua react
}
