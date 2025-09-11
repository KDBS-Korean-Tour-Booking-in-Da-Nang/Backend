package com.example.KDBS.dto.response;

import com.example.KDBS.enums.ReactionTargetType;
import com.example.KDBS.enums.ReactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReactionResponse {
    private Long reactionId;
    private ReactionType reactionType;
    private Long targetId;
    private ReactionTargetType targetType;
    private String username;
    private String userAvatar;
    private LocalDateTime createdAt;
}
