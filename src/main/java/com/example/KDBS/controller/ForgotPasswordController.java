package com.example.KDBS.controller;

import com.example.KDBS.dto.request.ForgotPasswordRequest;
import com.example.KDBS.dto.request.ResetPasswordRequest;
import com.example.KDBS.dto.response.ApiResponse;
import com.example.KDBS.service.ForgotPasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/forgot-password")
@RequiredArgsConstructor
@CrossOrigin("*")
@Slf4j
public class ForgotPasswordController {

    private final ForgotPasswordService forgotPasswordService;

    @PostMapping("/request")
    public ApiResponse<Void> requestPasswordReset(@Valid @RequestBody ForgotPasswordRequest request) {
        forgotPasswordService.requestPasswordReset(request);
        return ApiResponse.<Void>builder()
                .message("OTP sent successfully to your email")
                .build();
    }

    @PostMapping("/reset")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        forgotPasswordService.resetPassword(request);
        return ApiResponse.<Void>builder()
                .message("Password reset successfully")
                .build();
    }

    @PostMapping("/verify-otp")
    public ApiResponse<Boolean> verifyOTP(
            @RequestParam String email,
            @RequestParam String otpCode) {

        boolean isValid = forgotPasswordService.isOTPValid(email, otpCode);

        return ApiResponse.<Boolean>builder()
                .result(isValid)
                .message(isValid ? "OTP is valid" : "OTP is invalid")
                .build();
    }
}
