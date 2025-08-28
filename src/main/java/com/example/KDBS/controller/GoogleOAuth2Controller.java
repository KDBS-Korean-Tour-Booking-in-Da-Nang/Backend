package com.example.KDBS.controller;

import com.example.KDBS.dto.response.ApiResponse;
import com.example.KDBS.dto.response.AuthenticationResponse;
import com.example.KDBS.service.GoogleOAuth2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/auth/google")
@RequiredArgsConstructor
@CrossOrigin("*")
public class GoogleOAuth2Controller {

    private final GoogleOAuth2Service googleOAuth2Service;

    @GetMapping("/login")
    public ApiResponse<String> googleLogin() {
        // login -> redirect toi trang google auth
        String authUrl = googleOAuth2Service.getAuthorizationUrl();
        return ApiResponse.<String>builder()
                .code(1000)
                .result(authUrl)
                .build();
    }

    @GetMapping("/callback")
    public void googleCallback(@RequestParam("code") String code, HttpServletResponse response) throws IOException {
        try {
            AuthenticationResponse authResponse = googleOAuth2Service.handleGoogleCallback(code);

            // Redirect to frontend with user data
            String frontendUrl = "http://localhost:3000/google/callback?" +
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
            String errorUrl = "http://localhost:3000/google/callback?error="
                    + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            response.sendRedirect(errorUrl);
        }
    }
}