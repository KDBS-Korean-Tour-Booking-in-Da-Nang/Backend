package com.example.KDBS.utils;

import com.example.KDBS.enums.Status;
import com.example.KDBS.model.User;
import com.example.KDBS.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCleanupJobUtils {
    private final UserRepository userRepository;

    // chạy mỗi ngày lúc 2h sáng
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupUnverifiedUsers() {
        LocalDateTime expiry = LocalDateTime.now().minusDays(3);
        List<User> expired = userRepository.findByStatusAndCreatedAtBefore(Status.UNVERIFIED, expiry);

        userRepository.deleteAll(expired);
        log.info("Deleted {} expired unverified users", expired.size());
    }
}
