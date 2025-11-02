package com.example.KDBS.dto.request;

import com.example.KDBS.enums.ReactionTargetType;
import com.example.KDBS.enums.ReactionType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReactionRequest {
    @NotBlank(message = "User email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String userEmail;
    @NotNull(message = "Target ID is required")
    private Long targetId;
    @NotNull(message = "Target type is required")
    private ReactionTargetType targetType;
    @NotNull(message = "Reaction type is required")
    private ReactionType reactionType;
}
