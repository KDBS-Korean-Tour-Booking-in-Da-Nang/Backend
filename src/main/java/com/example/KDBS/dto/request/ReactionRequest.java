package com.example.KDBS.dto.request;

import com.example.KDBS.enums.ReactionTargetType;
import com.example.KDBS.enums.ReactionType;
import lombok.Data;

@Data
public class ReactionRequest {
    private String userEmail;
    private Long targetId;
    private ReactionTargetType targetType;
    private ReactionType reactionType;
}
