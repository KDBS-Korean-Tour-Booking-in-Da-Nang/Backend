package com.example.KDBS.controller;

import com.example.KDBS.dto.response.ApiResponse;
import com.example.KDBS.dto.response.NotificationSummaryResponse;
import com.example.KDBS.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin("*")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<NotificationSummaryResponse> getNotifications(
            @RequestHeader("User-Email") String userEmail,
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        Pageable pageable = buildPageable(page, size, sort);
        NotificationSummaryResponse summary = notificationService.getUserNotifications(userEmail, pageable, isRead);

        return ApiResponse.<NotificationSummaryResponse>builder()
                .result(summary)
                .build();
    }

    // Đánh dấu một thông báo là đã đọc
    @PutMapping("/{notificationId}/read")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> markAsRead(
            @PathVariable Long notificationId,
            @RequestHeader("User-Email") String userEmail) {

        notificationService.markAsRead(notificationId, userEmail);
        return ApiResponse.<Void>builder()
                .message("Notification marked as read")
                .build();
    }

    // Đánh dấu tất cả thông báo là đã đọc
    @PutMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> markAllAsRead(@RequestHeader("User-Email") String userEmail) {
        notificationService.markAllAsRead(userEmail);
        return ApiResponse.<Void>builder()
                .message("All notifications marked as read")
                .build();
    }

    // Xóa một thông báo
    @DeleteMapping("/{notificationId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> deleteNotification(
            @PathVariable Long notificationId,
            @RequestHeader("User-Email") String userEmail) {

        notificationService.deleteNotification(notificationId, userEmail);
        return ApiResponse.<Void>builder()
                .message("Notification deleted")
                .build();
    }

    private Pageable buildPageable(int page, int size, String sort) {
        String[] parts = sort.split(",");
        String field = parts[0];
        Sort.Direction direction = parts.length > 1 && parts[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(direction, field));
    }
}

