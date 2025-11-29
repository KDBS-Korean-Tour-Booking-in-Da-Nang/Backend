package com.example.KDBS.service;

import com.example.KDBS.enums.ArticleStatus;
import com.example.KDBS.enums.StaffTask;
import com.example.KDBS.enums.UserActionTarget;
import com.example.KDBS.enums.UserActionType;
import com.example.KDBS.model.Article;
import com.example.KDBS.repository.ArticleRepository;
import com.example.KDBS.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleService {
    private final ArticleRepository articleRepository;
    private final StaffService staffService;
    private final UserRepository userRepository;
    private final UserActionLogService userActionLogService;

    public List<Article> getAllArticles() {
        return articleRepository.findAll();
    }

    public Optional<Article> getArticleById(Long articleId) {
        return articleRepository.findById(articleId);
    }

    public List<Article> getArticlesByStatus(ArticleStatus status) {
        return articleRepository.findByArticleStatus(status);
    }

    public void logArticleRead(Article article, String userEmail) {
        try {
            if (article == null) {
                log.warn("Cannot log article read: article is null");
                return;
            }

            if (userEmail == null || userEmail.isBlank()) {
                log.debug("Cannot log article read: userEmail is null or blank for article {}", article.getArticleId());
                return;
            }

            log.debug("Attempting to log article read: articleId={}, userEmail={}", article.getArticleId(), userEmail);

            userRepository.findByEmail(userEmail).ifPresentOrElse(
                    user -> {
                        log.debug("Found user {} for email {}, logging article read", user.getUserId(), userEmail);
                        userActionLogService.logAction(
                                user,
                                UserActionType.READ_ARTICLE,
                                UserActionTarget.ARTICLE,
                                article.getArticleId(),
                                Map.of(
                                        "articleStatus",
                                        article.getArticleStatus() != null ? article.getArticleStatus().name()
                                                : "UNKNOWN",
                                        "articleTitle",
                                        article.getArticleTitle() != null ? article.getArticleTitle() : ""));
                        log.info("Successfully logged article read: articleId={}, userId={}", article.getArticleId(),
                                user.getUserId());
                    },
                    () -> log.warn("User not found for email: {}, cannot log article read for article {}", userEmail,
                            article.getArticleId()));
        } catch (Exception e) {
            log.error("Error logging article read for article {} and email {}",
                    article != null ? article.getArticleId() : "null", userEmail, e);
        }
    }

    @Transactional
    public Article updateArticleStatus(Long articleId, ArticleStatus newStatus) {
        staffService.getAuthorizedStaff(StaffTask.COMPANY_REQUEST_AND_APPROVE_ARTICLE);

        return articleRepository.findById(articleId)
                .map(article -> {
                    article.setArticleStatus(newStatus);
                    log.info("Updated article {} status to {}", articleId, newStatus);
                    return article;
                })
                .orElseGet(() -> {
                    log.warn("Article {} not found when trying to update status", articleId);
                    return null;
                });
    }

    public List<Article> crawlArticlesFromDanangXanh() {
        String baseUrl = "https://danangxanh.vn";
        String listArticlesUrl = baseUrl + "/tin-tuc.html";
        List<Article> crawledArticles = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(listArticlesUrl).get();

            // select all articles
            Elements articles = doc.select("li.tintuccat");

            List<String> links = new ArrayList<>();
            int count = 0;
            for (Element article : articles) {
                if (count >= 5)
                    break;
                String link = article.select("h3 > a").attr("abs:href").trim();

                if (!link.isEmpty()) {
                    links.add(link);
                    count++;
                }
            }

            Set<String> existingLinks = new HashSet<>(
                    articleRepository.findExistingArticleLinks(links));

            count = 0;
            for (Element article : articles) {
                if (count >= 5)
                    break;
                String link = article.select("h3 > a").attr("abs:href").trim();

                if (link.isEmpty()) {
                    continue;
                }

                // If we already have this article, stop crawling further
                // since danangxanh lists newest articles first
                if (existingLinks.contains(link)) {
                    log.info("Existing article found ({}). Stopping crawl early.", link);
                    break;
                }

                String title = article.select("h3 > a").text().trim();
                String thumbnail = article.select("div.img img").attr("abs:src").trim();

                String description = article.select("div:not([class])").stream()
                        .filter(div -> div.select("a").isEmpty())
                        .map(Element::text)
                        .findFirst()
                        .orElse("")
                        .trim();

                String content = extractDanangXanhArticleContent(link);

                Article newArticle = Article.builder()
                        .articleTitle(title)
                        .articleLink(link)
                        .articleThumbnail(thumbnail)
                        .articleDescription(description)
                        .articleContent(content)
                        .build();

                crawledArticles.add(newArticle);
                count++;
            }

            if (!crawledArticles.isEmpty()) {
                articleRepository.saveAll(crawledArticles);
                log.info("Saved {} new articles in batch", crawledArticles.size());
            }
        } catch (IOException e) {
            log.error("Error fetching articles from DanangXanh: {}", e.getMessage());
        }

        return crawledArticles;
    }

    private String extractDanangXanhArticleContent(String articleUrl) {
        try {
            Document doc = Jsoup.connect(articleUrl).get();

            StringBuilder contentBuilder = new StringBuilder();

            // Extract introtext
            Element introText = doc.selectFirst("div.introtext");
            if (introText != null) {
                contentBuilder.append(introText.outerHtml()).append("\n");
            }

            // Extract fulltext
            Element fullText = doc.selectFirst("div.fulltext");
            if (fullText != null) {
                contentBuilder.append(fullText.outerHtml());
            }

            return contentBuilder.toString();

        } catch (IOException e) {
            log.error("Error extracting content from article {}: {}", articleUrl, e.getMessage());
            return "";
        }
    }
}
