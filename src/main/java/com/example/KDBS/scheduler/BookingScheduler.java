package com.example.KDBS.scheduler;

import com.example.KDBS.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class BookingScheduler {
    private final BookingService bookingService;

    @Async
    @Scheduled(cron = "0 0 0 * * *")
    public void checkTourCompletions() {
        log.info("Running scheduled task: checkTourCompletions");
        bookingService.checkTourCompletionStatus();
    }
}
