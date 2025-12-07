package com.example.KDBS.controller;

import com.example.KDBS.dto.response.ApiResponse;
import com.example.KDBS.dto.response.AuthenticationResponse;
import com.example.KDBS.service.GoogleOAuth2Service;
import com.example.KDBS.service.NaverOAuth2Service;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin("*")
public class OAuth2Controller {

    private final GoogleOAuth2Service googleOAuth2Service;
    private final NaverOAuth2Service naverOAuth2Service;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    // Google OAuth2 endpoints
    @GetMapping("/google/login")
    public ApiResponse<String> googleLogin(@RequestParam(value = "platform", required = false) String platform) {
        String authUrl = googleOAuth2Service.getAuthorizationUrl(platform);
        return ApiResponse.<String>builder()
                .code(1000)
                .result(authUrl)
                .build();
    }

    @GetMapping("/google/callback")
    public void googleCallback(@RequestParam("code") String code,
                               @RequestParam(value = "state", required = false) String state,
                               HttpServletResponse response) throws IOException {
        String platform = "";
        if (state != null && state.startsWith("platform=")) {
            platform = state.substring("platform=".length());
        }

        handleOAuthCallback(
                () -> googleOAuth2Service.handleGoogleCallback(code),
                "google",
                platform,
                response
        );
    }

    // Naver OAuth2 endpoints
    @GetMapping("/naver/login")
    public ApiResponse<String> naverLogin() {
        String authUrl = naverOAuth2Service.getAuthorizationUrl();
        return ApiResponse.<String>builder()
                .code(1000)
                .result(authUrl)
                .build();
    }

    @GetMapping("/naver/callback")
    public void naverCallback(@RequestParam("code") String code,
                              @RequestParam("state") String state,
                              @RequestParam(value = "platform", required = false) String platform,
                              HttpServletResponse response) throws IOException {
        handleOAuthCallback(
                () -> naverOAuth2Service.handleNaverCallback(code, state),
                "naver",
                platform,
                response
        );
    }

    // Common callback handler
    private void handleOAuthCallback(OAuthCallbackHandler handler,
                                     String provider,
                                     String platform,
                                     HttpServletResponse response) throws IOException {
        try {
            AuthenticationResponse authResponse = handler.execute();

            String frontendUrl = buildFrontendUrl(provider, authResponse, platform);
            response.sendRedirect(frontendUrl);
        } catch (Exception e) {
            String errorUrl = frontendUrl + "/" + provider + "/callback?error="
                    + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            response.sendRedirect(errorUrl);
        }
    }

    private String buildFrontendUrl(String provider, AuthenticationResponse authResponse, String platform) {
        String frontendUrl;
        if ("mobile".equalsIgnoreCase(platform)) {
            frontendUrl = "mobilefe:/";
        }
        else {
            frontendUrl = this.frontendUrl;
        }

        return  frontendUrl + "/" + provider + "/callback?" +
                "token=" + URLEncoder.encode(authResponse.getToken(), StandardCharsets.UTF_8) +
                "&userId=" + authResponse.getUser().getUserId() +
                "&email=" + URLEncoder.encode(authResponse.getUser().getEmail(), StandardCharsets.UTF_8) +
                "&username=" + URLEncoder.encode(
                authResponse.getUser().getUsername() != null ? authResponse.getUser().getUsername() : "",
                StandardCharsets.UTF_8) +
                "&role=" + URLEncoder.encode(
                authResponse.getUser().getRole() != null ? authResponse.getUser().getRole() : "USER",
                StandardCharsets.UTF_8) +
                "&avatar=" + URLEncoder.encode(
                authResponse.getUser().getAvatar() != null ? authResponse.getUser().getAvatar() : "",
                StandardCharsets.UTF_8) +
                "&balance=" + (authResponse.getUser().getBalance() != null ? authResponse.getUser().getBalance() : "0");
    }

    @FunctionalInterface
    private interface OAuthCallbackHandler {
        AuthenticationResponse execute() throws Exception;
    }
}