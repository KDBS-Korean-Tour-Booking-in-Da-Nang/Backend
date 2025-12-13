package com.example.KDBS.scheduler;

import com.example.KDBS.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class UserScheduler {
    private final UserService userService;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Ho_Chi_Minh")
    public void checkTour() {
        log.info("Running scheduled task: reset user suggestion");
        userService.resetUserSuggestion();
        log.info("Completed task: reset user suggestion");
    }
}
