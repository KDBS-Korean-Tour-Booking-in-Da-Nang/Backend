package com.example.KDBS.config;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class GeminiConfig {
    @Value("${spring.ai.openai.api-key}")
    private String GROQ_API_KEY;

    @Value("${spring.ai.openai.base-url}")
    private String GROQ_API_URL;

    @Bean
    public OpenAiChatModel customGroqChatClient() {
        OpenAiApi groqOpenAiApi = new OpenAiApi.Builder()
                .apiKey(GROQ_API_KEY)
                .baseUrl(GROQ_API_URL)
                .build();
        return OpenAiChatModel.builder()
                .openAiApi(groqOpenAiApi)
                .build();
    }
}
