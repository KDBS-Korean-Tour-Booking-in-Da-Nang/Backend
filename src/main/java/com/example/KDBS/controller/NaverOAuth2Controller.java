package com.example.KDBS.controller;

import com.example.KDBS.dto.response.ApiResponse;
import com.example.KDBS.dto.response.AuthenticationResponse;
import com.example.KDBS.service.NaverOAuth2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
                .result(authUrl)
                .build();
    }

    @GetMapping("/callback")
    public ApiResponse<AuthenticationResponse> naverCallback(@RequestParam("code") String code,
                                                             @RequestParam("state") String state) {
        try {
            AuthenticationResponse response = naverOAuth2Service.handleNaverCallback(code, state);
            return ApiResponse.<AuthenticationResponse>builder()
                    .result(response)
                    .build();
        } catch (Exception e) {
            return ApiResponse.<AuthenticationResponse>builder()
                    .message("Naver login failed: " + e.getMessage())
                    .build();
        }
    }
}
