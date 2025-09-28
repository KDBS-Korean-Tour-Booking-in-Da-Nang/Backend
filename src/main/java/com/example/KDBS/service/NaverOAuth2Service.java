package com.example.KDBS.service;

import com.example.KDBS.dto.NaverUserInfo;
import com.example.KDBS.dto.response.AuthenticationResponse;
import com.example.KDBS.dto.response.UserResponse;
import com.example.KDBS.mapper.UserMapper;
import com.example.KDBS.enums.Role;
import com.example.KDBS.enums.Status;
import com.example.KDBS.model.User;
import com.example.KDBS.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

import static com.example.KDBS.enums.PremiumType.FREE;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverOAuth2Service {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;
    private final AuthenticationService authenticationService;
    private final UserMapper userMapper;

    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.naver.redirect-uri}")
    private String redirectUri;

    public String getAuthorizationUrl() {
        return "https://nid.naver.com/oauth2.0/authorize?" +
                "response_type=code" +
                "&client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&state=" + System.currentTimeMillis(); // state để chống CSRF
    }

    public AuthenticationResponse handleNaverCallback(String code, String state) {
        try {
            String accessToken = exchangeCodeForToken(code, state);
            NaverUserInfo userInfo = getUserInfoFromNaver(accessToken);

            User user = saveOrUpdateNaverUser(userInfo);
            String token = authenticationService.generateToken(user);

            // Convert User to UserResponse
            UserResponse userResponse = userMapper.toUserResponse(user);

            return AuthenticationResponse.builder()
                    .token(token)
                    .authenticated(true)
                    .user(userResponse)
                    .build();

        } catch (Exception e) {
            log.error("Error handling Naver callback: ", e);
            throw new RuntimeException("Naver authentication failed", e);
        }
    }

    private String exchangeCodeForToken(String code, String state) {
        String tokenUrl = "https://nid.naver.com/oauth2.0/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("code", code);
        body.add("state", state);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return (String) response.getBody().get("access_token");
        }
        throw new RuntimeException("Failed to exchange authorization code for access token (Naver)");
    }

    private NaverUserInfo getUserInfoFromNaver(String accessToken) {
        String userInfoUrl = "https://openapi.naver.com/v1/nid/me";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, Map.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> body = response.getBody();
            Map<String, Object> resp = (Map<String, Object>) body.get("response");

            NaverUserInfo userInfo = new NaverUserInfo();
            userInfo.setId((String) resp.get("id"));
            userInfo.setName((String) resp.get("name"));
            userInfo.setEmail((String) resp.get("email"));
            userInfo.setProfileImage((String) resp.get("profile_image"));
            return userInfo;
        }
        throw new RuntimeException("Failed to get user info from Naver");
    }

    private User saveOrUpdateNaverUser(NaverUserInfo userInfo) {
        String email = userInfo.getEmail();

        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (userInfo.getProfileImage() != null && !userInfo.getProfileImage().equals(user.getAvatar())) {
                user.setAvatar(userInfo.getProfileImage());
                userRepository.save(user);
            }
            return user;
        } else {
            User newUser = User.builder()
                    .email(email)
                    .username(userInfo.getName())
                    .avatar(userInfo.getProfileImage())
                    .password(passwordEncoder.encode("NAVER_OAUTH2_USER_" + System.currentTimeMillis()))
                    .status(Status.UNBANNED)
                    .role(Role.USER)
                    .premiumType(FREE)
                    .balance(java.math.BigDecimal.ZERO)
                    .build();
            return userRepository.save(newUser);
        }
    }
}
