package com.example.KDBS.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CozeChatService {

    @Value("${coze.api.base-url}")
    private String baseUrl;

    @Value("${coze.api.token}")
    private String cozeToken;

    @Value("${coze.api.bot-id}")
    private String botId;
    private final RestTemplate restTemplate = new RestTemplate();

    public String chat(String userId, String userMessage) {
        String url = baseUrl + "/chat";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(cozeToken);

        // body theo mẫu docs Coze
        Map<String, Object> body = Map.of(
                "bot_id", botId,
                "user_id", userId,
                "stream", false,
                "additional_messages", List.of(
                        Map.of(
                                "role", "user",
                                "content_type", "text",
                                "type", "question",
                                "content", userMessage
                        )
                )
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity(url, entity, String.class);

            log.info("Coze chat success, status={}", response.getStatusCode());
            return response.getBody(); // trả raw JSON, frontend tự parse
        } catch (Exception e) {
            log.error("Error calling Coze chat API", e);
            // tuỳ bạn muốn ném AppException hay RuntimeException
            throw new RuntimeException("Failed to call Coze API", e);
        }
    }
}
