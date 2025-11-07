package com.example.KDBS.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSummaryResponse {
    private Page<NotificationResponse> notifications;
    private Long unreadCount;
}

