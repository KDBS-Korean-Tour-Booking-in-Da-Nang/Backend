package com.example.KDBS.controller;

import com.example.KDBS.dto.response.ApiResponse;
import com.example.KDBS.dto.response.AuthenticationResponse;
import com.example.KDBS.service.GoogleOAuth2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
                .result(authUrl)
                .build();
    }

    @GetMapping("/callback")
    public ApiResponse<AuthenticationResponse> googleCallback(@RequestParam("code") String code) {
        try {
            AuthenticationResponse response = googleOAuth2Service.handleGoogleCallback(code);
            return ApiResponse.<AuthenticationResponse>builder()
                    .result(response)
                    .build();
        } catch (Exception e) {
            return ApiResponse.<AuthenticationResponse>builder()
                    .message("Google login failed: " + e.getMessage())
                    .build();
        }
    }
} 