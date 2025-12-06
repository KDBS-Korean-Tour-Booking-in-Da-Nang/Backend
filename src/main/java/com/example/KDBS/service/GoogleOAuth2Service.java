package com.example.KDBS.service;

import com.example.KDBS.dto.GoogleUserInfo;
import com.example.KDBS.dto.response.AuthenticationResponse;
import com.example.KDBS.dto.response.UserResponse;
import com.example.KDBS.enums.Role;
import com.example.KDBS.enums.Status;
import com.example.KDBS.mapper.UserMapper;
import com.example.KDBS.model.User;
import com.example.KDBS.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleOAuth2Service {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;
    private final AuthenticationService authenticationService;
    private final UserMapper userMapper;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    public String getAuthorizationUrl() {
        return "https://accounts.google.com/o/oauth2/v2/auth?" +
                "client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&scope=openid%20profile%20email" +
                "&response_type=code" +
                "&access_type=offline";
    }

    public AuthenticationResponse handleGoogleCallback(String code) {
        try {
            // exchange authorization code cho access token
            String accessToken = exchangeCodeForToken(code);

            GoogleUserInfo userInfo = getUserInfoFromGoogle(accessToken);

            User user = saveOrUpdateGoogleUser(userInfo);
            // gen jwt token bang authenservice
            String token = authenticationService.generateToken(user);

            // Convert User to UserResponse
            UserResponse userResponse = userMapper.toUserResponse(user);

            return AuthenticationResponse.builder()
                    .token(token)
                    .authenticated(true)
                    .user(userResponse)
                    .build();
        } catch (Exception e) {
            log.error("Error handling Google callback: ", e);
            throw new RuntimeException("Google authentication failed", e);
        }
    }

    private String exchangeCodeForToken(String code) {
        String tokenUrl = "https://oauth2.googleapis.com/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("code", code);
        body.add("grant_type", "authorization_code");
        body.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    tokenUrl,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<>() {
                    });
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (String) response.getBody().get("access_token");
            }
        } catch (Exception e) {
            log.error("Error exchanging code for token: ", e);
        }
        throw new RuntimeException("Failed to exchange authorization code for access token");
    }

    private GoogleUserInfo getUserInfoFromGoogle(String accessToken) {
        String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<GoogleUserInfo> response = restTemplate.exchange(
                    userInfoUrl,
                    HttpMethod.GET,
                    request,
                    GoogleUserInfo.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("Error getting user info from Google: ", e);
        }
        throw new RuntimeException("Failed to get user info from Google");
    }

    private User saveOrUpdateGoogleUser(GoogleUserInfo userInfo) {
        String email = userInfo.getEmail();

        Optional<User> existingUser = userRepository.findByEmail(email);
        // check user ton tai
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (!StringUtils.hasText(user.getAvatar())
                    && StringUtils.hasText(userInfo.getPicture())) {
                user.setAvatar(userInfo.getPicture());
                userRepository.save(user);
            }
            if (!StringUtils.hasText(user.getUsername())
                    && StringUtils.hasText(userInfo.getName())) {
                user.setUsername(userInfo.getName());
            }

            return user;
        } else {
            // tao user moi default = user
            User newUser = User.builder()
                    .email(email)
                    .username(userInfo.getName())
                    .avatar(userInfo.getPicture())
                    .password(passwordEncoder.encode("GOOGLE_OAUTH2_USER_" + System.currentTimeMillis()))
                    .status(Status.UNBANNED)
                    .role(Role.USER)
                    .balance(java.math.BigDecimal.ZERO)
                    .build();
            return userRepository.save(newUser);
        }
    }
}
