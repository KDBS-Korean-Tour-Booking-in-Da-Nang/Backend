package com.example.KDBS.service;

import com.example.KDBS.dto.response.TranslatedArticleResponse;
import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {
    private final Client client;

    public String askGemini(String prompt) {
        GenerateContentResponse response =
                client.models.generateContent(
                        "gemini-2.5-flash-lite",
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

    public TranslatedArticleResponse translateArticleToEnglishAndKorean(String title, String description, String content) {
        if (title == null || title.isEmpty() ||
            description == null || description.isEmpty() ||
            content == null || content.isEmpty()) {
            throw new AppException(ErrorCode.ARTICLE_FIELDS_EMPTY);
        }
        String prompt = """
                You are a professional Korean and English translator with over 10 years of experience
                           in localizing articles for diverse audiences. Translate the provided article into English and Korean.
                
                           Return ONLY a valid JSON object.
                           DO NOT wrap the JSON in backticks.
                           DO NOT add ```json or ``` at all.
                           DO NOT add any explanations, comments, or text outside the JSON.
                           Output MUST start with "{" and end with "}".
                
                           Use the following structure exactly:
                
                           {
                             "articleTitleEN": "",
                             "articleDescriptionEN": "",
                             "articleContentEN": "",
                             "articleTitleKR": "",
                             "articleDescriptionKR": "",
                             "articleContentKR": ""
                           }
                
                           Rules:
                           • Translate text only.
                           • Preserve all HTML tags exactly as they are.
                           • Preserve all formatting (line breaks, paragraphs).
                           • Do NOT translate HTML tags or special characters.
                           • Insert translated English content in *EN fields*.
                           • Insert translated Korean content in *KR fields*.
                
                           Original Article:
                           Title: %s
                           Description: %s
                           Content: %s
                
                """.formatted(title, description, content);
        String response = askGemini(prompt);
        response = cleanJson(response);
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(response, TranslatedArticleResponse.class);

        } catch (Exception ex) {
            log.info(response);
            throw new AppException(ErrorCode.AI_TRANSLATION_FAILED);
        }
    }

    private String cleanJson(String raw) {
        if (raw == null) return null;

        // Remove any markdown code fences
        raw = raw.replace("```json", "")
                .replace("```", "")
                .trim();

        // Extract JSON object between first { and last }
        int start = raw.indexOf("{");
        int end = raw.lastIndexOf("}");

        if (start >= 0 && end >= 0 && end > start) {
            raw = raw.substring(start, end + 1);
        }

        // Remove trailing commas (common LLM error)
        raw = raw.replaceAll(",\\s*}", "}")
                .replaceAll(",\\s*]", "]");

        return raw.trim();
    }
}
