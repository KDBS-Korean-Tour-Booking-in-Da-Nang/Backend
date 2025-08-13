package com.example.KDBS.service;

import com.example.KDBS.enums.OTPPurpose;
import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.example.KDBS.model.OTP;
import com.example.KDBS.repository.OTPRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class OTPService {

    private final OTPRepository otpRepository;
    private final EmailService emailService;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_OTP_ATTEMPTS_PER_HOUR = 3;

    public void generateAndSendOTP(String email, OTPPurpose purpose) {
        // check so lan gui OTP trong 1 h
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        List<OTP> recentOTPs = otpRepository.findRecentOTPs(email, purpose.name(), oneHourAgo);
        
        if (recentOTPs.size() >= MAX_OTP_ATTEMPTS_PER_HOUR) {
            throw new AppException(ErrorCode.TOO_MANY_OTP_REQUESTS);
        }

        // tao otp moi
        String otpCode = generateOTPCode();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(OTP_EXPIRY_MINUTES);

        // save OTP vào database
        OTP otp = OTP.builder()
                .email(email)
                .otpCode(otpCode)
                .createdAt(now)
                .expiresAt(expiresAt)
                .isUsed(false)
                .purpose(purpose.name())
                .build();

        otpRepository.save(otp);

        // gui otp qua mail
        emailService.sendOTPEmail(email, otpCode, purpose.name());

        log.info("OTP generated and sent to email: {} for purpose: {}", email, purpose);
    }

    public boolean verifyOTP(String email, String otpCode, OTPPurpose purpose) {
        LocalDateTime now = LocalDateTime.now();
        
        OTP otp = otpRepository.findValidOTP(email, purpose.name(), otpCode, now)
                .orElseThrow(() -> new AppException(ErrorCode.OTP_INVALID));

        // set OTP trang thai isUsed
        otp.setIsUsed(true);
        otpRepository.save(otp);

        log.info("OTP verified successfully for email: {} and purpose: {}", email, purpose);
        return true;
    }

    public void invalidateOTPs(String email, OTPPurpose purpose) {
        LocalDateTime now = LocalDateTime.now();
        List<OTP> validOTPs = otpRepository.findRecentOTPs(email, purpose.name(), now.minusDays(1));
        
        for (OTP otp : validOTPs) {
            if (!otp.getIsUsed() && otp.getExpiresAt().isAfter(now)) {
                otp.setIsUsed(true);
                otpRepository.save(otp);
            }
        }
        
        log.info("All valid OTPs invalidated for email: {} and purpose: {}", email, purpose);
    }

    private String generateOTPCode() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        
        return otp.toString();
    }

    public boolean isOTPExpired(String email, OTPPurpose purpose) {
        LocalDateTime now = LocalDateTime.now();
        return otpRepository.findLatestValidOTP(email, purpose.name(), now).isEmpty();
    }
} 