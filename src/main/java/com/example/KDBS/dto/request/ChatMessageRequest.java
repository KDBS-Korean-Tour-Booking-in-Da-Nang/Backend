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
    @NotBlank(message = "Sender name is required")
    String senderName;
    @NotBlank(message = "Receiver name is required")
    String receiverName;
    @NotBlank(message = "Content is required")
    String content;
}
