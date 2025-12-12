package com.example.KDBS.service;
import com.example.KDBS.dto.response.SuggestTourResultResponse;
import com.example.KDBS.enums.*;
import com.example.KDBS.model.*;
import com.example.KDBS.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SuggestTourViaBehaviorService {
    private final UserRepository userRepository;
    private final UserActionLogRepository logRepository;
    private final ForumPostRepository postRepository;
    private final ForumCommentRepository commentRepository;
    private final SuggestedTourRepository suggestedTourRepository;
    private final TourRepository tourRepository;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    @Transactional
    public List<Tour> suggestTours(int userId) {

        // 1. Dù userId hợp lệ hay không, vẫn cho phép suggest
        User user = userRepository.findById(userId).orElse(null);

        // 2. Nếu user tồn tại và đã suggest hôm nay → trả về suggestion cũ
        if (user != null && user.getSuggestion() == SuggestionStatus.SUGGESTED) {
            log.info("User {} already received a suggestion today → return the previous suggestion", userId);

            List<SuggestedTour> saved = suggestedTourRepository.findByUser(user);

            return saved.stream()
                    .map(SuggestedTour::getTour)
                    .toList();
        }

        // 3. Nếu user hợp lệ → xóa suggestion cũ để ghi lại mới
        if (user != null) {
            suggestedTourRepository.deleteByUser(user);
        }

        // 4. Lấy logs để suggest
        List<UserActionLog> logs;

        if (user != null) {
            logs = logRepository.findTop30ByUserOrderByCreatedAtDesc(user);
        } else {
            logs = logRepository.findTop30ByOrderByCreatedAtDesc();
        }

        if (logs.isEmpty()) return List.of();

        // Extract all user interest texts
        List<String> texts = extractUserInterestTexts(logs);
        if (texts.isEmpty()) return List.of();

        List<Tour> tours = tourRepository.findAllPublicTours(TourStatus.PUBLIC);
        if (tours.isEmpty()) return List.of();

        // 5. Build & send prompt
        String prompt = buildPrompt(texts, tours);
        String aiResponse = geminiService.askGemini(prompt, "groq/compound");
        String cleaned = cleanJson(aiResponse);

        SuggestTourResultResponse result;
        try {
            result = objectMapper.readValue(cleaned, SuggestTourResultResponse.class);
        } catch (Exception ex) {
            log.error("Failed to parse JSON", ex);
            return List.of();
        }

        // 6. Convert IDs → Tour list
        List<Tour> recommendedTours = result.getRecommendedTourIds().stream()
                .map(tourRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        // 7. Nếu user tồn tại → lưu suggestion + đánh dấu đã suggest hôm nay
        if (user != null) {
            for (Tour t : recommendedTours) {
                SuggestedTour st = SuggestedTour.builder()
                        .user(user)
                        .tour(t)
                        .build();
                suggestedTourRepository.save(st);
            }

            user.setSuggestion(SuggestionStatus.SUGGESTED);
            userRepository.save(user);
        }

        return recommendedTours;
    }


    /**
     * Extract texts from all related user activities
     */
    private List<String> extractUserInterestTexts(List<UserActionLog> logs) {
        List<String> result = new ArrayList<>();

        for (UserActionLog log : logs) {

            try {
                JsonNode metadata = objectMapper.readTree(log.getMetadataJson());
                String text;

                switch (log.getActionType()) {

                    case CREATE_POST:
                        text = metadata.path("title").asText("") + " " + metadata.path("content").asText("");
                        result.add(text);
                        break;

                    case CREATE_COMMENT:
                        text = metadata.path("content").asText("");
                        result.add(text);
                        break;

                    case ADD_REACTION:
                        ReactionType reactionType = ReactionType.valueOf(metadata.path("reactionType").asText("LIKE"));

                        if (reactionType == ReactionType.LIKE) {
                            if (log.getTargetType() == UserActionTarget.POST) {
                                postRepository.findById(log.getTargetId()).ifPresent(
                                        post -> result.add(post.getTitle() + " " + post.getContent())
                                );
                            } else if (log.getTargetType() == UserActionTarget.COMMENT) {
                                commentRepository.findById(log.getTargetId()).ifPresent(
                                        c -> result.add(c.getContent())
                                );
                            }
                        }
                        break;

                    case READ_ARTICLE:
                        String title = metadata.path("articleTitle").asText("");
                        String summary = metadata.path("articleSummary").asText("");
                        result.add(title + " " + summary);
                        break;
                }

            } catch (Exception ignore) {}
        }

        return result;
    }


    private String buildPrompt(List<String> texts, List<Tour> tours) {

        StringBuilder prompt = new StringBuilder("""
                You are KDBS AI Recommendation Engine.
                                
                Your task:
                    1. Analyze what the user is interested in based on the articles they read,
                       the forum posts they created, the comments they wrote, and the posts or comments they liked.
                                
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
                    - If the database contains **4 or fewer tours**, you MUST return *all available tourIds* exactly as provided.
                    - If the database contains **more than 4 tours**, you MUST ALWAYS return at least 4 tourIds.
                    - If user interest signals have strong matches → return the most relevant tours.
                    - If user preferences are unclear → return 4 tours that are the most generally relevant based on:
                        * popularity,
                        * well-known destinations,
                        * scenic appeal,
                        * versatility for general travelers.
                    - NEVER return fewer than 4 tourIds (unless total DB tours ≤ 4).
                    - NEVER add explanation or natural language. Only return a JSON object exactly as shown.
                                
                ### USER INTEREST CONTENT:
                """);

        for (String t : texts) {
            prompt.append("- ").append(t.replace("\"", "'")).append("\n");
        }

        prompt.append("\n### AVAILABLE TOURS:\n");

        for (Tour tour : tours) {
            prompt.append("""
               {
                 "tourId": %d,
                 "tourName": "%s",
                 "tourDescription": "%s",
                 "tourDuration": "%s"
               }
               """.formatted(
                    tour.getTourId(),
                    safe(tour.getTourName()),
                    safe(tour.getTourDescription()),
                    safe(tour.getTourDuration())
            ));
        }

        prompt.append("""
            Choose the most relevant tours.
            Return only JSON.
            """);

        return prompt.toString();
    }


    private String safe(String s) {
        return s == null ? "" : s.replace("\"", "'");
    }

    private String cleanJson(String raw) {
        if (raw == null) return null;

        raw = raw.replace("```json", "")
                .replace("```", "")
                .trim();

        int start = raw.indexOf("{");
        int end = raw.lastIndexOf("}");

        if (start >= 0 && end > start) {
            raw = raw.substring(start, end + 1);
        }

        raw = raw.replaceAll(",\\s*}", "}")
                .replaceAll(",\\s*]", "]");

        return raw.trim();
    }
}
