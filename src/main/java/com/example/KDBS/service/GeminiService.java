package com.example.KDBS.service;

import com.example.KDBS.dto.response.TranslatedArticleResponse;
import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {
    private final OpenAiChatModel customGroqChatClient;

    private static final String TRAVEL_ASSISTANT_SYSTEM_PROMPT =
            """
                    My system is a management platform for booking Da Nang tours targeted at Korean tourists.
                    You will act as the AI assistant for this system to answer user questions from both tourists and companies. If users ask about operational steps or system terms, you will explain them clearly.
                    Here are the key concepts you need to learn:
                    
                    Tour Name: The name of the tour. You should proactively suggest naming it in Korean to reach more users.
                    Durations and Nights: These indicate the days and nights of the tour, for example, 3 days and 2 nights, or 4 days and 3 nights.
                    Number of available tours: The number of tours the company wants to offer on the system.
                    Booking Cut-off Date: The date after which the tour can no longer be booked, meaning the company stops offering it on the system. However, any bookings made before this date will still proceed normally.
                    Check Day: The estimated time the company needs to process documents on the system. This can vary based on each company's actual operations.
                    Balance Payment Day: The deadline for users to pay the remaining balance of their booking.
                    Minimum Advance Day: This is the required lead time before the departure date for the company to approve documents and for the user to pay the remaining balance. It's calculated as Minimum Advance Day = Check Day + Balance Payment Day. This only starts after the user pays the deposit. After the Check Date, the user has (Balance Payment Day) days to pay the remaining amount. If not, the booking will automatically fail and be refunded according to specific rules.
                    Deposit Percentage: The percentage of the deposit that the company sets for the tours they create.
                    If a user cancels the tour within the Check Day period, they will receive a 100% refund.
                    If a user cancels the tour within the Balance Payment Day period, they will receive an 80% refund.
                    Allow refund after balance payment: If the company does not select this option, users who cancel after paying the remaining balance will not get a refund. If the company selects it and fills in the Refund Floor, users can still get a refund calculated as: Refund percentage = (Number of days remaining before departure / Minimum Advance Day) * 100. The refund amount will range from 80% down to the Refund Floor.
                    Refund Floor: The minimum refund percentage that the company will provide to users.
                    If users inquire about terms that haven't been translated into Korean on the system, translate them for the users.
                    If users complain about the system, guide them to create a ticket in the bottom-left corner of the homepage.
                    Respond in the same language as the incoming question.
                    Answer must be friendly and professional like a tour guide. A little humor is allowed. Suitable for korean's senser of humor
                    
                    """;

    public String askGemini(String prompt, String model) {
        int maxRetries = 5;
        int attempt = 0;

        while (true) {
            try {
                ChatOptions chatOptions = OpenAiChatOptions.builder()
                        .model(model)
                        .build();
                return customGroqChatClient.call(new Prompt(prompt, chatOptions))
                        .getResult()
                        .getOutput()
                        .getText();
            } catch (ResourceAccessException e) {
                attempt++;
                log.info("Attempt {} failed: {}", attempt, e.getMessage());
                if (attempt >= maxRetries) {
                    throw new RuntimeException("Failed after " + maxRetries + " attempts", e);
                }
            }
        }
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

        return askGemini(prompt, "llama-3.3-70b-versatile");
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
                       Add \\" to escape all double quotes within string values.
                       Output MUST start with "{" and end with "}".
            
                       CRITICAL: All string values MUST be on a single line with NO line breaks.
                       Replace all newline characters with spaces or remove them entirely.
                       Ensure the entire JSON is valid and can be parsed without errors.
            
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
                       DO NOT include any newlines or line breaks within the HTML content strings.
                       Keep all HTML on a single continuous line.
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
        String response = askGemini(prompt, "llama-3.1-8b-instant");
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
        return askGemini(prompt.toString(), "llama-3.1-8b-instant");
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

        // Remove trailing commas
        raw = raw.replaceAll(",\\s*}", "}")
                .replaceAll(",\\s*]", "]");

        // Try to parse and re-serialize to fix escaping issues
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature());
            mapper.enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER.mappedFeature());

            JsonNode node = mapper.readTree(raw);
            return mapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            // If Jackson can't parse it, try manual quote fixing
            return fixUnescapedQuotesManually(raw);
        }
    }

    private String fixUnescapedQuotesManually(String json) {
        // Use the fixUnescapedQuotes method from above
        StringBuilder result = new StringBuilder();
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (escaped) {
                result.append(c);
                escaped = false;
                continue;
            }

            if (c == '\\') {
                result.append(c);
                escaped = true;
                continue;
            }

            if (c == '"') {
                if (!inString) {
                    result.append(c);
                    inString = true;
                } else {
                    // Check if this is really the end of the string
                    int j = i + 1;
                    while (j < json.length() && Character.isWhitespace(json.charAt(j))) {
                        j++;
                    }

                    if (j < json.length()) {
                        char next = json.charAt(j);
                        if (next == ':' || next == ',' || next == '}' || next == ']') {
                            result.append(c);
                            inString = false;
                        } else {
                            result.append("\\\"");
                        }
                    } else {
                        result.append(c);
                        inString = false;
                    }
                }
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }
}
