package com.loopins.core.config;

import com.loopins.core.domain.entity.User;
import com.loopins.core.domain.enums.UserRole;
import com.loopins.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataSeederConfig {

    private final UserRepository userRepository;

    /**
     * Seeds sample data for development and testing.
     */
    @Bean
    @Profile({"dev", "test"})
    public CommandLineRunner seedData() {
        return args -> {
            if (userRepository.count() == 0) {
                log.info("Seeding sample users...");

                User customer1 = User.builder()
                        .username("john_doe")
                        .email("john@example.com")
                        .role(UserRole.CUSTOMER)
                        .build();

                User customer2 = User.builder()
                        .username("jane_smith")
                        .email("jane@example.com")
                        .role(UserRole.CUSTOMER)
                        .build();

                User seller = User.builder()
                        .username("acme_store")
                        .email("store@acme.com")
                        .role(UserRole.SELLER)
                        .build();

                User admin = User.builder()
                        .username("admin")
                        .email("admin@loopins.com")
                        .role(UserRole.ADMIN)
                        .build();

                userRepository.save(customer1);
                userRepository.save(customer2);
                userRepository.save(seller);
                userRepository.save(admin);

                log.info("Sample users seeded successfully");
            }
        };
    }
}

