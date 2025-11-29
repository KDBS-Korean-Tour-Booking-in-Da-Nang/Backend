package com.example.KDBS.service;

import com.example.KDBS.dto.response.NotificationResponse;
import com.example.KDBS.dto.response.NotificationSummaryResponse;
import com.example.KDBS.enums.NotificationType;
import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.example.KDBS.mapper.NotificationMapper;
import com.example.KDBS.model.Notification;
import com.example.KDBS.model.User;
import com.example.KDBS.repository.NotificationRepository;
import com.example.KDBS.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Transactional
    public NotificationResponse pushNotification(Integer recipientUserId,
                                                 Integer actorUserId,
                                                 NotificationType type,
                                                 String title,
                                                 String message,
                                                 Long targetId,
                                                 String targetType) {
        if (recipientUserId == null) {
            log.warn("Recipient is null, skip notification {}", type);
            return null;
        }
        User recipient = userRepository.findById(recipientUserId)
                .orElse(null);
        if (recipient == null) {
            log.warn("Recipient {} not found, skip notification {}", recipientUserId, type);
            return null;
        }
        User actor = actorUserId != null
                ? userRepository.findById(actorUserId).orElse(null)
                : null;

        Notification notification = Notification.builder()
                .recipient(recipient)
                .actor(actor)
                .notificationType(type)
                .title(title)
                .message(message)
                .targetId(targetId)
                .targetType(targetType)
                .build();
        Notification saved = notificationRepository.save(notification);
        NotificationResponse response = notificationMapper.toResponse(saved);
        sendThroughSocket(recipient, response);
        return response;
    }

    @Transactional(readOnly = true)
    public NotificationSummaryResponse getNotifications(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Page<NotificationResponse> page = notificationRepository.findByRecipientOrderByCreatedAtDesc(user, pageable)
                .map(notificationMapper::toResponse);
        long unreadCount = notificationRepository.countByRecipientAndIsReadIsFalse(user);
        return NotificationSummaryResponse.builder()
                .notifications(page)
                .unreadCount(unreadCount)
                .build();
    }

    @Transactional
    public void markAsRead(Long notificationId, String email) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));
        if (!notification.getRecipient().getEmail().equals(email)) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }
        notification.setIsRead(true);
    }

    @Transactional(readOnly = true)
    public long countUnread(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return notificationRepository.countByRecipientAndIsReadIsFalse(user);
    }

    private void sendThroughSocket(User recipient, NotificationResponse response) {
        try {
            simpMessagingTemplate.convertAndSendToUser(
                    recipient.getUsername(),
                    "/queue/notifications",
                    response
            );
        } catch (Exception e) {
            log.error("Failed to send notification via socket to {}", recipient.getUsername(), e);
        }
    }
}