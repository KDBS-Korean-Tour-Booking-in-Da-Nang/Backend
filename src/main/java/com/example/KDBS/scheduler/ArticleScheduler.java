package com.example.KDBS.scheduler;

import com.example.KDBS.service.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleScheduler {
    private final ArticleService articleService;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Ho_Chi_Minh")
    public void crawlDanangxanhArticle() {
        log.info("Auto crawling: Crawling Danangxanh articles...");
        articleService.crawlArticlesFromDanangXanh();
        log.info("Auto crawling: Finished crawling Danangxanh articles.");
    }
}
