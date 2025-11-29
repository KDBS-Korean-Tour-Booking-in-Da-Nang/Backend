package com.example.KDBS.mapper;

import com.example.KDBS.dto.response.NotificationResponse;
import com.example.KDBS.model.Notification;
import com.example.KDBS.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface NotificationMapper {

    @Mapping(target = "actor", expression = "java(buildActorInfo(notification.getActor()))")
    NotificationResponse toResponse(Notification notification);

    default NotificationResponse.ActorInfo buildActorInfo(User actor) {
        if (actor == null) {
            return null;
        }
        return NotificationResponse.ActorInfo.builder()
                .userId(actor.getUserId())
                .username(actor.getUsername())
                .avatar(actor.getAvatar())
                .build();
    }
}


