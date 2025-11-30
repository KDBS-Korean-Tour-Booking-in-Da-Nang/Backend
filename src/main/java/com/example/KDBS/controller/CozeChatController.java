package com.example.KDBS.controller;

import com.example.KDBS.dto.request.CozeChatRequest;
import com.example.KDBS.service.CozeChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin("*")
public class CozeChatController {

    private final CozeChatService cozeChatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/send")              // FE gửi về /app/send
    public void processMessage(CozeChatRequest msg) {

        // gửi thông báo đang xử lý
        messagingTemplate.convertAndSend(
                "/topic/reply/" + msg.getUserEmail(),
                "⏳ Bot đang phản hồi..."
        );

        // gọi Coze (kiểu STREAM hoặc Poll theo từng chunk)
        cozeChatService.streamAnswer(msg.getUserEmail(), msg.getMessage(), chunk -> {
            // chunk là một phần câu trả lời
            messagingTemplate.convertAndSend(
                    "/topic/reply/" + msg.getUserEmail(),
                    chunk
            );
        });

        // hoàn tất
        messagingTemplate.convertAndSend(
                "/topic/reply/" + msg.getUserEmail(),
                "✔️ Bot trả lời xong."
        );
    }
}
