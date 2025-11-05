package com.example.KDBS.mapper;

import com.example.KDBS.dto.response.NotificationResponse;
import com.example.KDBS.model.Notification;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface NotificationMapper {

    @Mapping(target = "actor", ignore = true)
    NotificationResponse toNotificationResponse(Notification notification);

    @AfterMapping
    default void mapActorInfo(@MappingTarget NotificationResponse response, Notification notification) {
        if (notification.getActor() != null) {
            NotificationResponse.ActorInfo actorInfo = NotificationResponse.ActorInfo.builder()
                    .userId(notification.getActor().getUserId())
                    .username(notification.getActor().getUsername())
                    .avatar(notification.getActor().getAvatar())
                    .build();
            response.setActor(actorInfo);
        }
    }
}

