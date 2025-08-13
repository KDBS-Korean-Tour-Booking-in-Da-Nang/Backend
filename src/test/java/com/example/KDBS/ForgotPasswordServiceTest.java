package com.example.KDBS;

import com.example.KDBS.dto.request.ForgotPasswordRequest;
import com.example.KDBS.dto.request.ResetPasswordRequest;
import com.example.KDBS.enums.OTPPurpose;
import com.example.KDBS.enums.Status;
import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.example.KDBS.model.User;
import com.example.KDBS.repository.UserRepository;
import com.example.KDBS.service.ForgotPasswordService;
import com.example.KDBS.service.OTPService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ForgotPasswordServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OTPService otpService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ForgotPasswordService forgotPasswordService;

    private User testUser;
    private ForgotPasswordRequest forgotPasswordRequest;
    private ResetPasswordRequest resetPasswordRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(1)
                .email("test@example.com")
                .username("testuser")
                .status(Status.UNBANNED)
                .build();

        forgotPasswordRequest = ForgotPasswordRequest.builder()
                .email("test@example.com")
                .build();

        resetPasswordRequest = ResetPasswordRequest.builder()
                .email("test@example.com")
                .otpCode("123456")
                .newPassword("newpassword123")
                .build();
    }

    @Test
    void requestPasswordReset_Success() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        doNothing().when(otpService).generateAndSendOTP(anyString(), any(OTPPurpose.class));

        // When
        assertDoesNotThrow(() -> forgotPasswordService.requestPasswordReset(forgotPasswordRequest));

        // Then
        verify(otpService).generateAndSendOTP("test@example.com", OTPPurpose.FORGOT_PASSWORD);
    }

    @Test
    void requestPasswordReset_EmailNotFound() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // When & Then
        AppException exception = assertThrows(AppException.class, 
            () -> forgotPasswordService.requestPasswordReset(forgotPasswordRequest));
        assertEquals(ErrorCode.EMAIL_NOT_EXISTED, exception.getErrorCode());
    }

    @Test
    void requestPasswordReset_UserBanned() {
        // Given
        testUser.setStatus(Status.BANNED);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When & Then
        AppException exception = assertThrows(AppException.class, 
            () -> forgotPasswordService.requestPasswordReset(forgotPasswordRequest));
        assertEquals(ErrorCode.USER_IS_BANNED, exception.getErrorCode());
    }

    @Test
    void resetPassword_Success() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(otpService.verifyOTP(anyString(), anyString(), any(OTPPurpose.class))).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedpassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        assertDoesNotThrow(() -> forgotPasswordService.resetPassword(resetPasswordRequest));

        // Then
        verify(otpService).verifyOTP("test@example.com", "123456", OTPPurpose.FORGOT_PASSWORD);
        verify(passwordEncoder).encode("newpassword123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void resetPassword_InvalidOTP() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(otpService.verifyOTP(anyString(), anyString(), any(OTPPurpose.class)))
            .thenThrow(new AppException(ErrorCode.OTP_INVALID));

        // When & Then
        AppException exception = assertThrows(AppException.class, 
            () -> forgotPasswordService.resetPassword(resetPasswordRequest));
        assertEquals(ErrorCode.OTP_INVALID, exception.getErrorCode());
    }
} 