package com.example.KDBS.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessageRequest {
    int senderId;
    int receiverId;
    @NotBlank(message = "Content is required")
    String content;
}
