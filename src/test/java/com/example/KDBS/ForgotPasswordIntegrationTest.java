package com.example.KDBS;

import com.example.KDBS.dto.request.ForgotPasswordRequest;
import com.example.KDBS.dto.request.ResetPasswordRequest;
import com.example.KDBS.enums.Status;
import com.example.KDBS.model.User;
import com.example.KDBS.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
class ForgotPasswordIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private User testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Tạo test user
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password(passwordEncoder.encode("oldpassword"))
                .status(Status.UNBANNED)
                .build();
        userRepository.save(testUser);
    }

    @Test
    void testForgotPasswordFlow() throws Exception {
        // 1. Yêu cầu đặt lại mật khẩu
        ForgotPasswordRequest forgotRequest = ForgotPasswordRequest.builder()
                .email("test@example.com")
                .build();

        mockMvc.perform(post("/api/auth/forgot-password/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(forgotRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("OTP sent successfully to your email"));

        // 2. Xác thực OTP (giả sử OTP là "123456")
        mockMvc.perform(post("/api/auth/forgot-password/verify-otp")
                .param("email", "test@example.com")
                .param("otpCode", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(true));

        // 3. Đặt lại mật khẩu
        ResetPasswordRequest resetRequest = ResetPasswordRequest.builder()
                .email("test@example.com")
                .otpCode("123456")
                .newPassword("newpassword123")
                .build();

        mockMvc.perform(post("/api/auth/forgot-password/reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resetRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Password reset successfully"));
    }

    @Test
    void testForgotPasswordWithNonExistentEmail() throws Exception {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .email("nonexistent@example.com")
                .build();

        mockMvc.perform(post("/api/auth/forgot-password/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testForgotPasswordWithBannedUser() throws Exception {
        // Ban user
        testUser.setStatus(Status.BANNED);
        userRepository.save(testUser);

        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .email("test@example.com")
                .build();

        mockMvc.perform(post("/api/auth/forgot-password/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testResetPasswordWithInvalidOTP() throws Exception {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .email("test@example.com")
                .otpCode("000000")
                .newPassword("newpassword123")
                .build();

        mockMvc.perform(post("/api/auth/forgot-password/reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
} 