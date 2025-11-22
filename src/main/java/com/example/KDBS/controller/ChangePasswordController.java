package com.example.KDBS.controller;

import com.example.KDBS.dto.request.ChangePasswordRequest;
import com.example.KDBS.dto.response.ApiResponse;
import com.example.KDBS.service.ChangePasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/change-password")
@RequiredArgsConstructor
@CrossOrigin("*")
@Slf4j
public class ChangePasswordController {
    private final ChangePasswordService changePasswordService;

    @PostMapping()
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        try {
            changePasswordService.changePassword(request);
            return ApiResponse.<Void>builder()
                    .message("Password changed successfully")
                    .build();
        } catch (Exception e) {
            log.error("Error changing password for email: {}", request.getEmail(), e);
            return ApiResponse.<Void>builder()
                    .message("Failed to change password: " + e.getMessage())
                    .build();
        }
    }

}
