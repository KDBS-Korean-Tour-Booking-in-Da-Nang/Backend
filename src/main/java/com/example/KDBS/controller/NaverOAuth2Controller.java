package com.example.KDBS.controller;

import com.example.KDBS.dto.response.ApiResponse;
import com.example.KDBS.dto.response.AuthenticationResponse;
import com.example.KDBS.service.NaverOAuth2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/auth/naver")
@RequiredArgsConstructor
@CrossOrigin("*")
public class NaverOAuth2Controller {

    private final NaverOAuth2Service naverOAuth2Service;

    @GetMapping("/login")
    public ApiResponse<String> naverLogin() {
        String authUrl = naverOAuth2Service.getAuthorizationUrl();
        return ApiResponse.<String>builder()
                .code(1000)
                .result(authUrl)
                .build();
    }

    @GetMapping("/callback")
    public void naverCallback(@RequestParam("code") String code,
            @RequestParam("state") String state,
            HttpServletResponse response) throws IOException {
        try {
            AuthenticationResponse authResponse = naverOAuth2Service.handleNaverCallback(code, state);

            // Redirect to frontend with user data
            String frontendUrl = "http://localhost:3000/naver/callback?" +
                    "token=" + URLEncoder.encode(authResponse.getToken(), StandardCharsets.UTF_8) +
                    "&userId=" + authResponse.getUser().getUserId() +
                    "&email=" + URLEncoder.encode(authResponse.getUser().getEmail(), StandardCharsets.UTF_8) +
                    "&username="
                    + URLEncoder.encode(
                            authResponse.getUser().getUsername() != null ? authResponse.getUser().getUsername() : "",
                            StandardCharsets.UTF_8)
                    +
                    "&role="
                    + URLEncoder.encode(
                            authResponse.getUser().getRole() != null ? authResponse.getUser().getRole() : "USER",
                            StandardCharsets.UTF_8)
                    +
                    "&avatar="
                    + URLEncoder.encode(
                            authResponse.getUser().getAvatar() != null ? authResponse.getUser().getAvatar() : "",
                            StandardCharsets.UTF_8)
                    +
                    "&isPremium="
                    + (authResponse.getUser().getIsPremium() != null ? authResponse.getUser().getIsPremium() : false) +
                    "&balance="
                    + (authResponse.getUser().getBalance() != null ? authResponse.getUser().getBalance() : "0");

            response.sendRedirect(frontendUrl);
        } catch (Exception e) {
            // Redirect to frontend with error
            String errorUrl = "http://localhost:3000/naver/callback?error="
                    + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            response.sendRedirect(errorUrl);
        }
    }
}
