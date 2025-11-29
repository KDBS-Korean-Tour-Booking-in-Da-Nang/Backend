package com.example.KDBS.dto.response;

import com.example.KDBS.enums.UserActionTarget;
import com.example.KDBS.enums.UserActionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActionLogResponse {
    private Long logId;
    private UserActionType actionType;
    private UserActionTarget targetType;
    private Long targetId;
    private String metadataJson;
    private LocalDateTime createdAt;
}





