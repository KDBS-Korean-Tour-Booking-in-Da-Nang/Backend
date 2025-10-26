package com.example.KDBS.mapper;

import com.example.KDBS.dto.response.ChatMessageResponse;
import com.example.KDBS.model.ChatMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChatMessageMapper {

    // Map entity -> DTO
    @Mapping(source = "sender.username", target = "senderName")
    @Mapping(source = "receiver.username", target = "receiverName")
    ChatMessageResponse toResponse(ChatMessage chatMessage);
}
