package com.example.KDBS.service;

import com.example.KDBS.dto.response.SuggestTourResultResponse;
import com.example.KDBS.enums.TourStatus;
import com.example.KDBS.enums.UserActionTarget;
import com.example.KDBS.enums.UserActionType;
import com.example.KDBS.model.*;
import com.example.KDBS.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SuggestTourByArticleService {

    private final UserRepository userRepository;
    private final UserActionLogRepository logRepository;
    private final ArticleRepository articleRepository;
    private final TourRepository tourRepository;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;
    private final SuggestedTourRepository suggestedTourRepository;

    /**
     * Suggest tour based on user article reading behavior.
     * If email invalid → fallback to 10 latest logs of READ_ARTICLE.
     */
    public List<Tour> suggestToursForUser(int userId) {

        User user = userRepository.findById(userId).orElse(null);

        List<UserActionLog> logs;

        if (user != null) {
            logs = logRepository.findByUserAndActionTypeAndTargetType(
                    user,
                    UserActionType.READ_ARTICLE,
                    UserActionTarget.ARTICLE
            );
        } else {
            logs = logRepository.findTop10ByActionTypeAndTargetTypeOrderByCreatedAtDesc(
                    UserActionType.READ_ARTICLE,
                    UserActionTarget.ARTICLE
            );
        }

        if (logs.isEmpty()) {
            return List.of();
        }

        List<Article> articles = logs.stream()
                .map(log -> articleRepository.findById(log.getTargetId()).orElse(null))
                .filter(Objects::nonNull)
                .toList();

        if (articles.isEmpty()) {
            return List.of();
        }

        List<Tour> tours = tourRepository.findAllByTourStatusIn(List.of(TourStatus.PUBLIC));

        if (tours.isEmpty()) {
            return List.of();
        }

        // Build AI prompt
        String prompt = buildPrompt(articles, tours);

        String aiResponse = geminiService.askGemini(prompt, "groq/compound-mini");

        String cleaned = cleanJson(aiResponse);

        // Parse ai response
        SuggestTourResultResponse result;
        try {
            result = objectMapper.readValue(cleaned, SuggestTourResultResponse.class);
        } catch (Exception ex) {
            log.error("Failed to parse AI JSON", ex);
            return List.of();
        }

        // Convert IDs → List<Tour>
        List<Tour> recommendedTours = result.getRecommendedTourIds().stream()
                .map(tourRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        if (user != null) {
            for (Tour t : recommendedTours) {
                SuggestedTour st = SuggestedTour.builder()
                        .user(user)
                        .tour(t)
                        .build();
                suggestedTourRepository.save(st);
            }
        }

        return recommendedTours;
    }


    private String buildPrompt(List<Article> articles, List<Tour> tours) {

        StringBuilder prompt = new StringBuilder("""
               You are KDBS AI Recommendation Engine.
                                       
               Your task:
                   1. Analyze what the user is interested in based on the articles they read.
               
                   2. Determine user preferences such as:
                        - preferred destinations (beach, mountains, historical sites, city tours)
                        - activity type (cultural, adventure, sightseeing, spiritual)
                        - duration preference (short vs. long tours)
                        - scenic preferences (landscapes, beaches, night markets, rural areas)
               
                   3. Match these preferences with the list of tours provided later.
               
                   4. Output ONLY a pure JSON object:
                   {
                     "recommendedTourIds": [1, 5, 7, 10]
                   }
               
               IMPORTANT RULES:
                   - You MUST ALWAYS return at least 4 tourIds in the array.
                   - If user interest signals strongly match specific tours → choose those tours.
                   - If no clear match exists → choose 4 tours that are most relevant based on:
                       * general travel popularity,
                       * well-known destinations,
                       * scenic appeal,
                       * neutral or widely appealing tour types.
                   - NEVER return fewer than 4 tourIds.
                   - NEVER output explanations or natural language.
               
               ### USER INTEREST CONTENT:
               
                """);

        for (Article a : articles) {
            prompt.append("""
                ---
                Title: %s
                Summary: %s
                ---
                """.formatted(
                    safe(a.getArticleTitle()),
                    safe(a.getArticleSummary())
            ));
        }

        prompt.append("\n### AVAILABLE TOURS (reduced fields):\n");

        for (Tour t : tours) {
            prompt.append("""
                {
                   "tourId": %d,
                   "tourName": "%s",
                   "tourDescription": "%s",
                   "tourDuration": "%s"
                }
                """.formatted(
                    t.getTourId(),
                    safe(t.getTourName()),
                    safe(t.getTourDescription()),
                    safe(t.getTourDuration())
            ));
        }

        prompt.append("""
                
            Select the most relevant tours.
            Return ONLY JSON. 
            """);

        return prompt.toString();
    }

    private String safe(String s) {
        return s == null ? "" : s.replace("\"", "'");
    }

    private String cleanJson(String raw) {
        if (raw == null) return null;

        // remove markdown fences
        raw = raw.replace("```json", "")
                .replace("```", "")
                .trim();

        // extract only the json object
        int start = raw.indexOf("{");
        int end = raw.lastIndexOf("}");

        if (start >= 0 && end >= 0 && end > start) {
            raw = raw.substring(start, end + 1);
        }

        // remove trailing commas
        raw = raw.replaceAll(",\\s*}", "}")
                .replaceAll(",\\s*]", "]");

        return raw.trim();
    }

}
