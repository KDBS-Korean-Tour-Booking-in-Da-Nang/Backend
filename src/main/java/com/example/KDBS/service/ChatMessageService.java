package com.example.KDBS.service;

import com.example.KDBS.dto.request.ChatMessageRequest;
import com.example.KDBS.model.ChatMessage;
import com.example.KDBS.model.User;
import com.example.KDBS.repository.ChatMessageRepository;
import com.example.KDBS.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    public List<ChatMessage> GetConversation(String senderName, String receiverName) {
        var sender = getUserByUsername(senderName);
        var receiver = getUserByUsername(receiverName);

        return chatMessageRepository.findBySenderAndReceiverOrReceiverAndSenderOrderByTimestampAsc(
                sender, receiver,
                receiver, sender);
    }

    public List<ChatMessage> getAllMessageFromUser(String Username) {
        var user = getUserByUsername(Username);
        return chatMessageRepository.findBySenderOrReceiverOrderByTimestampDesc(user, user);
    }

    public List<ChatMessage> sendMessage(ChatMessageRequest chatMessageRequest) {
        var sender = getUserByUsername(chatMessageRequest.getSenderName());
        var receiver = getUserByUsername(chatMessageRequest.getReceiverName());
        ChatMessage newMessage = ChatMessage.builder()
                .sender(sender)
                .receiver(receiver)
                .content(chatMessageRequest.getContent())
                .build();
        chatMessageRepository.save(newMessage);
        log.info("Message sent from {} to {}", chatMessageRequest.getSenderName(), chatMessageRequest.getReceiverName());
        return GetConversation(chatMessageRequest.getSenderName(), chatMessageRequest.getReceiverName());
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", username);
                    return new RuntimeException("User not found: " + username);
                });
    }
}
