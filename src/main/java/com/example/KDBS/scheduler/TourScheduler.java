package com.example.KDBS.scheduler;

import com.example.KDBS.service.TourService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class TourScheduler {
    private final TourService tourService;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Ho_Chi_Minh")
    public void checkTour() {
        log.info("Running scheduled task: check for tour expire");
        tourService.checkTour();
        log.info("Completed task: check for tour expire");
    }
}
