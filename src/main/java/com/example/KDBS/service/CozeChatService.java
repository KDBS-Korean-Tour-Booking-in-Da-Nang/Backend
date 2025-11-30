package com.example.KDBS.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Slf4j
public class CozeChatService {

    private final RestTemplate restTemplate;

    @Value("${coze.api.base-url}")
    private String baseUrl;

    @Value("${coze.api.token}")
    private String token;

    @Value("${coze.api.bot-id}")
    private String botId;

    public void streamAnswer(String userId, String query, Consumer<String> callback) {
        String chatId = createChat(userId, query);

        String status;
        do {
            status = getChatStatus(chatId);
            callback.accept("…");
            sleep(600);
        } while (!"completed".equals(status));

        String answer = getFinalAnswer(chatId);

        for (String w : answer.split(" ")) {
            callback.accept(w + " ");
            sleep(50);
        }
    }

    private String createChat(String userId, String query) {
        String url = baseUrl + "/chat";

        HttpHeaders h = headers();
        Map<String, Object> body = Map.of(
                "bot_id", botId,
                "user_id", userId,
                "query", query
        );

        var res = restTemplate.postForObject(url, new HttpEntity<>(body, h), Map.class);
        return ((Map<?, ?>) res.get("data")).get("id").toString();
    }

    private String getChatStatus(String chatId) {
        String url = baseUrl + "/chat/retrieve?chat_id=" + chatId;

        var res = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers()), Map.class).getBody();
        return ((Map<?, ?>) res.get("data")).get("status").toString();
    }

    private String getFinalAnswer(String chatId) {
        String url = baseUrl + "/chat/messages?chat_id=" + chatId;
        var res = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers()), Map.class).getBody();
        var msgs = (List<Map<String, Object>>) res.get("data");

        return msgs.stream()
                .filter(m -> "assistant".equals(m.get("role")))
                .map(m -> m.get("content").toString())
                .reduce("", (a, b) -> a + b);
    }

    private HttpHeaders headers() {
        HttpHeaders h = new HttpHeaders();
        h.set("Authorization", "Bearer " + token);
        return h;
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (Exception ignored) {}
    }
}
