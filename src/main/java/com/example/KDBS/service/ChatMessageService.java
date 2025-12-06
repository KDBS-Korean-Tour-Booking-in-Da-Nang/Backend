package com.example.KDBS.service;

import com.example.KDBS.dto.request.ChatMessageRequest;
import com.example.KDBS.dto.response.ChatMessageResponse;
import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.example.KDBS.mapper.ChatMessageMapper;
import com.example.KDBS.model.ChatMessage;
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
    public List<ChatMessageResponse> GetConversation(int senderId, int receiverId) {
        var sender = userRepository.findById(senderId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        var receiver = userRepository.findById(receiverId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return chatMessageRepository.findConversationBetween(sender, receiver)
                .stream()
                .map(chatMessageMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getAllMessageFromUser(int userId) {
        var user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return chatMessageRepository.findBySenderOrReceiverOrderByTimestampDesc(user, user)
                .stream()
                .map(chatMessageMapper::toResponse)
                .toList();
    }

    @Transactional
    public List<ChatMessageResponse> sendMessage(ChatMessageRequest chatMessageRequest) {
        var sender = userRepository.findById(chatMessageRequest.getSenderId()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        var receiver = userRepository.findById(chatMessageRequest.getReceiverId()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        ChatMessage newMessage = ChatMessage.builder()
                .sender(sender)
                .receiver(receiver)
                .content(chatMessageRequest.getContent())
                .build();
        chatMessageRepository.save(newMessage);
        log.info("Message sent from {} to {}", sender.getUsername(), receiver.getUsername());
        return GetConversation(chatMessageRequest.getSenderId(), chatMessageRequest.getReceiverId());
    }
}
