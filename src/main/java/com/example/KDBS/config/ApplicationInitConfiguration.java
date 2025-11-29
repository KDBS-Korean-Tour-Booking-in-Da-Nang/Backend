package com.example.KDBS.config;

import com.example.KDBS.enums.Role;
import com.example.KDBS.enums.Status;
import com.example.KDBS.model.User;
import com.example.KDBS.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class ApplicationInitConfiguration {

    private final PasswordEncoder passwordEncoder;
    @Bean
    @ConditionalOnProperty(
            prefix = "spring.datasource",
            value = "driver-class-name",
            havingValue = "com.mysql.cj.jdbc.Driver"
    )
    ApplicationRunner applicationRunner(UserRepository userRepository) {
        log.info("Initializing application.....");
        return args -> {
            // Nếu hệ thống chưa có tài khoản ADMIN nào thì tạo default admin/admin
            boolean hasAdmin = userRepository.findAll().stream()
                    .anyMatch(user -> user.getRole() == Role.ADMIN);

            if (!hasAdmin) {
                User admin = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
                        .role(Role.ADMIN)
                        .status(Status.UNBANNED)
                        .createdAt(LocalDateTime.now())
                        .build();

                userRepository.save(admin);
                log.warn("Default admin user has been created with username='admin' and password='admin'. Please change it.");
            }

            log.info("Application initialization completed .....");
        };
    }
}
