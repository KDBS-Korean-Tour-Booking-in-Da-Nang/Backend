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
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationMapper notificationMapper;


    @Transactional
    public Notification createNotification(
            Integer recipientUserId,
            Integer actorUserId,
            NotificationType type,
            Long targetId,
            String targetType,
            String title,
            String message) {

        if (actorUserId != null && actorUserId.equals(recipientUserId)) {
            log.debug("Skipping self-notification for user {}", recipientUserId);
            return null;
        }

        User recipient = userRepository.findById(recipientUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        User actor = actorUserId != null
                ? userRepository.findById(actorUserId).orElse(null)
                : null;

        Notification notification = Notification.builder()
                .user(recipient)
                .actor(actor)
                .notificationType(type)
                .title(title)
                .message(message)
                .targetId(targetId)
                .targetType(targetType)
                .isRead(false)
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Notification created: {} for user {}", type, recipientUserId);

        sendNotificationViaWebSocket(saved);
        return saved;
    }

    private void sendNotificationViaWebSocket(Notification notification) {
        try {
            NotificationResponse response = notificationMapper.toNotificationResponse(notification);
            String userEmail = notification.getUser().getEmail();

            messagingTemplate.convertAndSendToUser(
                    userEmail,
                    "/queue/notifications",
                    response
            );
            messagingTemplate.convertAndSend("/topic/notifications", response);

            log.debug("WebSocket notification sent to user: {}", userEmail);
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification to user {}: {}",
                    notification.getUser().getEmail(), e.getMessage(), e);
        }
    }


    @Transactional(readOnly = true)
    public NotificationSummaryResponse getUserNotifications(String userEmail, Pageable pageable, Boolean isRead) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Page<Notification> notifications;
        if (isRead != null) {
            notifications = notificationRepository.findByUserAndIsReadOrderByCreatedAtDesc(user, isRead, pageable);
        } else {
            notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        }

        Long unreadCount = notificationRepository.countByUserAndIsReadFalse(user);

        return NotificationSummaryResponse.builder()
                .notifications(notifications.map(notificationMapper::toNotificationResponse))
                .unreadCount(unreadCount)
                .build();
    }

    @Transactional
    public void markAsRead(Long notificationId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (notification.getUser().getUserId() != user.getUserId()) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        notificationRepository.markAsRead(notificationId, user);
    }

    @Transactional
    public void markAllAsRead(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        notificationRepository.markAllAsReadByUser(user);
    }

    @Transactional
    public void deleteNotification(Long notificationId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (notification.getUser().getUserId() != user.getUserId()) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        notificationRepository.delete(notification);
    }
}

