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
    private String senderName;
    private String receiverName;
    private String content;
    private LocalDateTime timestamp;
}
