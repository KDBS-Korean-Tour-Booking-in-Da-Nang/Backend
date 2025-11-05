package com.example.KDBS.scheduler;

import com.example.KDBS.repository.InvalidateTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@RequiredArgsConstructor
@Slf4j
@Component
public class TokenCleanupJobScheduler {
    private final InvalidateTokenRepository invalidateTokenRepository;

    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupExpiredTokens() {
        invalidateTokenRepository.deleteByExpiryTimeBefore(new Date());
    }
}
