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
    private int senderId;
    private int receiverId;
    private String content;
    private LocalDateTime timestamp;
}
