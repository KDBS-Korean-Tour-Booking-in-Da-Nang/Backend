package com.example.KDBS.service;

import com.example.KDBS.dto.response.SuggestTourResultResponse;
import com.example.KDBS.enums.TourStatus;
import com.example.KDBS.model.*;
import com.example.KDBS.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SuggestTourByArticleService {

    private final ArticleRepository articleRepository;
    private final TourRepository tourRepository;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;
    public List<Tour> suggestToursByArticle(Long articleId) {

        // CASE 1: Không có articleId → lấy 4 tour PUBLIC gần nhất
        if (articleId == null || articleId == 0) {
            return tourRepository
                    .findTop4ByTourStatusOrderByCreatedAtDesc(TourStatus.PUBLIC);
        }

        // CASE 2: Có articleId → lấy article
        Article article = articleRepository.findById(articleId).orElse(null);

        if (article == null) {
            // articleId không tồn tại → fallback
            return tourRepository
                    .findTop4ByTourStatusOrderByCreatedAtDesc(TourStatus.PUBLIC);
        }

        List<Tour> tours = tourRepository.findAllByTourStatusIn(
                List.of(TourStatus.PUBLIC)
        );

        if (tours.isEmpty()) {
            return List.of();
        }

        // Build prompt từ 1 article
        String prompt = buildPrompt(article, tours);

        String aiResponse = geminiService.askGemini(
                prompt,
                "llama-3.1-8b-instant"
        );

        String cleaned = cleanJson(aiResponse);

        SuggestTourResultResponse result;
        try {
            result = objectMapper.readValue(cleaned, SuggestTourResultResponse.class);
        } catch (Exception ex) {
            log.error("Failed to parse AI JSON", ex);
            return List.of();
        }

        return result.getRecommendedTourIds().stream()
                .map(tourRepository::findById)
                .flatMap(Optional::stream)
                .filter(t -> t.getTourStatus() == TourStatus.PUBLIC)
                .toList();
    }




    private String buildPrompt(Article article, List<Tour> tours) {

        StringBuilder prompt = new StringBuilder("""
        You are KDBS AI Recommendation Engine.

        Your task:
            1. Analyze the user's interest based on the article content below.

            2. Determine preferences such as:
                - destination type (beach, mountains, historical, city)
                - activity type (cultural, adventure, sightseeing, spiritual)
                - tour duration (short or long)
                - scenery preference (nature, beach, night market, rural)

            3. Match these preferences with the available tours.

        Output format (STRICT):
        {
          "recommendedTourIds": [1, 5, 7, 10]
        }

        IMPORTANT RULES:
            - ALWAYS return at least 4 tourIds.
            - ONLY return tourIds that exist in the provided tour list.
            - NEVER return explanations or additional text.

        ### ARTICLE CONTENT:
        Title: %s
        Summary: %s

        ### AVAILABLE TOURS:
        """.formatted(
                safe(article.getArticleTitleEN()),
                safe(article.getArticleSummary())
        ));

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
        Select the 4 most relevant tours.
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
