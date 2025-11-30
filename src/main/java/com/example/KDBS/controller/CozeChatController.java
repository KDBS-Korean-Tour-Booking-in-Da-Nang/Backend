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

    @MessageMapping("/send")  // FE gửi tới /app/send
    public void processMessage(CozeChatRequest msg) {
        if (msg == null || msg.getMessage() == null || msg.getMessage().isBlank()) {
            return;
        }
        if (msg.getClientId() == null || msg.getClientId().isBlank()) {
            return;
        }

        String clientId = msg.getClientId();
        String destination = "/topic/ai/reply/" + clientId;

        // thông báo đang xử lý
        messagingTemplate.convertAndSend(destination, "⏳ Bot đang phản hồi...");

        // gọi Coze, stream từng chunk
        cozeChatService.streamAnswer(clientId, msg.getMessage(), chunk -> {
            messagingTemplate.convertAndSend(destination, chunk);
        });

        // hoàn tất
        messagingTemplate.convertAndSend(destination, "✔️ Bot trả lời xong.");
    }
}
