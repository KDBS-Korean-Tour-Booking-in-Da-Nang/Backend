package com.example.KDBS.service;

import com.example.KDBS.dto.request.ForgotPasswordRequest;
import com.example.KDBS.dto.request.ResetPasswordRequest;
import com.example.KDBS.enums.OTPPurpose;
import com.example.KDBS.enums.Status;
import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.example.KDBS.model.User;
import com.example.KDBS.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForgotPasswordService {

    private final UserRepository userRepository;
    private final OTPService otpService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public void requestPasswordReset(ForgotPasswordRequest request) {
        // check mail trong db
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_EXISTED));

        // check banned
        if (Status.BANNED.equals(user.getStatus())) {
            throw new AppException(ErrorCode.USER_IS_BANNED);
        }

        // gui otp qua mail
        otpService.generateAndSendOTP(request.getEmail(), OTPPurpose.FORGOT_PASSWORD);

        log.info("Password reset OTP sent to email: {}", request.getEmail());
    }

    public void resetPassword(ResetPasswordRequest request) {
        // check mail trong db
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_EXISTED));

        // check banned
        if (Status.BANNED.equals(user.getStatus())) {
            throw new AppException(ErrorCode.USER_IS_BANNED);
        }

        // verify otp
        try {
            otpService.verifyOTP(request.getEmail(), request.getOtpCode(), OTPPurpose.FORGOT_PASSWORD);
        } catch (AppException e) {
            if (ErrorCode.OTP_INVALID.equals(e.getErrorCode())) {
                throw new AppException(ErrorCode.OTP_INVALID);
            }
            throw e;
        }

        // set password moi
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encodedPassword);
        userRepository.save(user);

        // gui mail thong bao doi pass thanh cong
        emailService.sendPasswordResetSuccessEmail(request.getEmail(), user.getUsername());

        // xoa otp cu
        otpService.invalidateOTPs(request.getEmail(), OTPPurpose.FORGOT_PASSWORD);

        log.info("Password reset successfully for email: {}", request.getEmail());
    }

    public boolean isOTPValid(String email, String otpCode) {
        try {
            return otpService.verifyOTP(email, otpCode, OTPPurpose.FORGOT_PASSWORD);
        } catch (Exception e) {
            return false;
        }
    }
}