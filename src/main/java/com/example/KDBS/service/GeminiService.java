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

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {
    private final Client client;

    private static final String TRAVEL_ASSISTANT_SYSTEM_PROMPT =
            "You are KDBS AI Travel Assistant — a friendly and knowledgeable virtual tour guide. " +
                    "You have 10+ years of expertise in Vietnamese tourism, especially Da Nang, Hoi An, Hue, and nearby attractions. " +
                    "Your job is to help users plan tours, choose the best travel dates, understand ticket prices, suggest itineraries, " +
                    "advise what to pack, and provide helpful tips about weather, transportation, and local culture. " +
                    "Always reply concisely, naturally, and supportively, like a real tour guide. " +
                    "If the user is unsure, ask clarifying questions. " +
                    "Never mention that you are an AI model—act like an expert tour guide. " +
                    "Do NOT provide medical, legal, or financial advice. " +
                    "Now start assisting the user based on their message.\n\n";

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
                             "articleContentKR": "",
                             "articleSummary": ""
                           }
                
                           Rules:
                           Translate text only.
                           Preserve all HTML tags exactly as they are.
                           Preserve all formatting (line breaks, paragraphs).
                           Do NOT translate HTML tags or special characters.
                           Insert translated English content in *EN fields*.
                           Insert translated Korean content in *KR fields*.
                
                           Summarize the article in English in 2-3 sentences,
                           include the location that the article talk about and key information that would help analyze user interests
                           and improve personalized tour recommendations, without adding full descriptions. Place the result in the "articleSummary" field.
                
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

    public String chatSession(List<String> history, String newMessage) {

        StringBuilder prompt = new StringBuilder(TRAVEL_ASSISTANT_SYSTEM_PROMPT);

        // Ghép lịch sử của session (do frontend gửi xuống)
        if (history != null) {
            for (String msg : history) {
                prompt.append("User: ").append(msg).append("\n");
            }
        }

        // Thêm câu mới
        prompt.append("User: ").append(newMessage).append("\n");
        prompt.append("Guide:");

        // Gửi vào askGemini
        return askGemini(prompt.toString());
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
