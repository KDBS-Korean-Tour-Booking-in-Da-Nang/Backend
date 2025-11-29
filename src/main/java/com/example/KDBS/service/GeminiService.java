package com.example.KDBS.service;

import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GeminiService {
    private final Client client;

    public String askGemini(String prompt) {
        GenerateContentResponse response =
                client.models.generateContent(
                        "gemini-2.5-flash",
                        prompt,
                        null);

        return response.text();
    }

    public String translateText(String text) {
        if (text == null || text.isEmpty()) {
            throw new AppException(ErrorCode.TEXT_IS_EMPTY);
        }
        String prompt =
                "You are a professional Korean translator with 10+ years of experience in localizing online community content " +
                        "with a deep understanding of both formal and casual language, internet slang, and forum communication styles. " +
                        "Translate the following text into Korean in a casual, conversational style suitable for forum discussions. " +
                        "Preserve ALL quotation marks exactly as they appear, including any phrases inside \"...\". " +
                        "Do NOT remove, change, or omit quoted parts—translate them naturally into Korean while keeping the quotes. " +
                        "Handle slang or informal expressions naturally. " +
                        "Return ONLY the translated Korean text with no explanations or commentary.\n\n" +
                        "Text to translate: \"" + text + "\"";

        return askGemini(prompt);
    }
}
