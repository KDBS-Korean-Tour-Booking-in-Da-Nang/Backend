package com.example.KDBS.controller;

import com.example.KDBS.dto.request.ChatMessageRequest;
import com.example.KDBS.dto.response.ChatMessageResponse;
import com.example.KDBS.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatMessageController {
    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @GetMapping("/conversation/{user1}/{user2}")
    public ResponseEntity<List<ChatMessageResponse>> getConversation(@PathVariable String user1, @PathVariable String user2) {
        List<ChatMessageResponse> conversation = chatMessageService.GetConversation(user1, user2);
        return ResponseEntity.ok(conversation);
    }

    @GetMapping("/all/{username}")
    public ResponseEntity<List<ChatMessageResponse>> getAllMessagesFromUser(@PathVariable String username) {
        List<ChatMessageResponse> messages = chatMessageService.getAllMessageFromUser(username);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/send")
    public ResponseEntity<List<ChatMessageResponse>> sendMessage(@RequestBody ChatMessageRequest chatMessageRequest) {
        List<ChatMessageResponse> updatedConversation = chatMessageService.sendMessage(chatMessageRequest);
        return ResponseEntity.ok(updatedConversation);
    }

    //WebSocket endpoint to send message
    @MessageMapping("/chat.send")
    public void processMessage(@Payload ChatMessageRequest chatMessageRequest){
        log.info("Received WS message: {}", chatMessageRequest);

        //Notify for receiver
        simpMessagingTemplate.convertAndSendToUser(
                chatMessageRequest.getReceiverName(),
                "/queue/messages",
                chatMessageRequest
        );
        //Notify for sender
        simpMessagingTemplate.convertAndSendToUser(
                chatMessageRequest.getSenderName(),
                "/queue/messages",
                chatMessageRequest
        );

        chatMessageService.sendMessage(chatMessageRequest);
    }
}
