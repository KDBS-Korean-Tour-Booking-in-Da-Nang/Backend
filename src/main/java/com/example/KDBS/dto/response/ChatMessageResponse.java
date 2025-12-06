package com.example.KDBS.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageResponse {
    private Long messageId;
    private String senderId;
    private String receiverId;
    private String content;
    private LocalDateTime timestamp;
}
