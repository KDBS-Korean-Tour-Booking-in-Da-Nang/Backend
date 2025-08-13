package com.example.KDBS.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${spring.application.name:KDBS}")
    private String appName;

    public void sendOTPEmail(String toEmail, String otpCode, String purpose) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Mã xác thực " + appName);

            // tao context cho thymleaf
            Context context = new Context();
            context.setVariable("otpCode", otpCode);
            context.setVariable("purposeText", getPurposeText(purpose));
            
            // render template
            String emailContent = templateEngine.process("email/otp-email", context);
            helper.setText(emailContent, true);

            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send OTP email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String getPurposeText(String purpose) {
        switch (purpose) {
            case "FORGOT_PASSWORD":
                return "đặt lại mật khẩu";
            case "VERIFY_EMAIL":
                return "xác thực email";
            default:
                return "xác thực";
        }
    }

    public void sendPasswordResetSuccessEmail(String toEmail, String username) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Đặt lại mật khẩu thành công - " + appName);

            // tao context cho thymleaf
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("appName", appName);
            
            // render template
            String emailContent = templateEngine.process("email/password-reset-success", context);
            helper.setText(emailContent, true);

            mailSender.send(message);
            log.info("Password reset success email sent to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send password reset success email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
} 