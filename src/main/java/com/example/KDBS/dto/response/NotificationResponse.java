package com.example.KDBS.dto.response;

import com.example.KDBS.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long notificationId;
    private NotificationType notificationType;
    private String title;
    private String message;
    private Long targetId;
    private String targetType;
    private Boolean isRead;
    private LocalDateTime createdAt;
    
    // Thông tin người thực hiện hành động (actor)
    private ActorInfo actor;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActorInfo {
        private Integer userId;
        private String username;
        private String avatar;
    }
}

