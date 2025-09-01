package com.example.KDBS.dto.request;

import com.example.KDBS.enums.ReactionTargetType;
import com.example.KDBS.enums.ReactionType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReactionRequest {
    
    @NotNull(message = "Target ID is required")
    private Long targetId;
    
    @NotNull(message = "Target type is required")
    private ReactionTargetType targetType; // POST hoặc COMMENT
    
    @NotNull(message = "Reaction type is required")
    private ReactionType reactionType;
}
