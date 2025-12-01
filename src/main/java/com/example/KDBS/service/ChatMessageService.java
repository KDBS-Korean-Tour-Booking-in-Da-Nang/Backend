package com.example.KDBS.service;

import com.example.KDBS.dto.request.ChatMessageRequest;
import com.example.KDBS.dto.response.ChatMessageResponse;
import com.example.KDBS.mapper.ChatMessageMapper;
import com.example.KDBS.model.ChatMessage;
import com.example.KDBS.model.User;
import com.example.KDBS.repository.ChatMessageRepository;
import com.example.KDBS.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ChatMessageMapper chatMessageMapper;

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> GetConversation(String senderName, String receiverName) {
        var sender = getUserByUsername(senderName);
        var receiver = getUserByUsername(receiverName);

        return chatMessageRepository.findConversationBetween(sender, receiver)
                .stream()
                .map(chatMessageMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getAllMessageFromUser(String Username) {
        var user = getUserByUsername(Username);
        return chatMessageRepository.findBySenderOrReceiverOrderByTimestampDesc(user, user)
                .stream()
                .map(chatMessageMapper::toResponse)
                .toList();
    }

    @Transactional
    public List<ChatMessageResponse> sendMessage(ChatMessageRequest chatMessageRequest) {
        var sender = getUserByUsername(chatMessageRequest.getSenderName());
        var receiver = getUserByUsername(chatMessageRequest.getReceiverName());
        ChatMessage newMessage = ChatMessage.builder()
                .sender(sender)
                .receiver(receiver)
                .content(chatMessageRequest.getContent())
                .build();
        chatMessageRepository.save(newMessage);
        log.info("Message sent from {} to {}", chatMessageRequest.getSenderName(),
                chatMessageRequest.getReceiverName());
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
