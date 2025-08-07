package com.example.KDBS.config;

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

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class ApplicationInitConfiguration {

    PasswordEncoder passwordEncoder;
    @Bean
    @ConditionalOnProperty(
            prefix = "spring",
            value = "datasource.driverClassName",
            havingValue = "com.mysql.cj.jdbc.Driver")
    ApplicationRunner applicationRunner(UserRepository userRepository) {
        log.info("Initializing application.....");
        return args -> {
//            xu ly tao admin neu he thong k co tai khoan
//            if (userRepository.findByUserName("admin").isEmpty()){
//                User user = User.builder()
//                        .userName("admin")
//                        .userEmail("admin")
//                        .userPassword(passwordEncoder.encode("admin"))
//                        .userRole(Role.ADMIN.name())
//                        .userStatus("noStatus")
//                        .build();
//                userRepository.save(user);
//                log.warn("admin user has been created with default password: admin, please change it.");
//            }
            log.info("Application initialization completed .....");
        };
    }
}
