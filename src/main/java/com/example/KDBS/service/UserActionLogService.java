package com.example.KDBS.service;

import com.example.KDBS.dto.response.UserActionLogResponse;
import com.example.KDBS.enums.Role;
import com.example.KDBS.enums.UserActionTarget;
import com.example.KDBS.enums.UserActionType;
import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.example.KDBS.mapper.UserActionLogMapper;
import com.example.KDBS.model.User;
import com.example.KDBS.model.UserActionLog;
import com.example.KDBS.repository.UserActionLogRepository;
import com.example.KDBS.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserActionLogService {

    private final UserActionLogRepository userActionLogRepository;
    private final UserRepository userRepository;
    private final UserActionLogMapper userActionLogMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public void logAction(User user,
                          UserActionType actionType,
                          UserActionTarget targetType,
                          Long targetId,
                          Map<String, Object> metadata) {

        if (!shouldLog(user)) {
            log.debug("Skipping log action: user role is not USER. userId={}, role={}", 
                    user != null ? user.getUserId() : "null", 
                    user != null ? user.getRole() : "null");
            return;
        }

        try {
            String metadataJson = metadata != null ? objectMapper.writeValueAsString(metadata) : null;
            UserActionLog logEntry = UserActionLog.builder()
                    .user(user)
                    .actionType(actionType)
                    .targetType(targetType)
                    .targetId(targetId)
                    .metadataJson(metadataJson)
                    .build();
            UserActionLog saved = userActionLogRepository.save(logEntry);
            log.debug("Successfully saved action log: logId={}, userId={}, actionType={}, targetType={}, targetId={}", 
                    saved.getLogId(), user.getUserId(), actionType, targetType, targetId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize metadata for action log: userId={}, actionType={}", 
                    user != null ? user.getUserId() : "null", actionType, e);
        } catch (Exception e) {
            log.error("Unexpected error saving action log: userId={}, actionType={}, targetType={}, targetId={}", 
                    user != null ? user.getUserId() : "null", actionType, targetType, targetId, e);
        }
    }

    @Transactional(readOnly = true)
    public Page<UserActionLogResponse> getLogsForUser(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userActionLogRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(userActionLogMapper::toResponse);
    }

    private boolean shouldLog(User user) {
        return user != null && user.getRole() == Role.USER;
    }
}





