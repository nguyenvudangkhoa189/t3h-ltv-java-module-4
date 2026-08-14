package vn.demo.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.demo.document.User;
import vn.demo.repository.UserRepository;

import java.util.List;

/**
 * CONFIG — seed user/admin nếu DB trống (lab).
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Seed initial user data on application startup
     */
    @Bean
    public ApplicationRunner seedUsers() {
        return args -> {
            // --- Tránh seed trùng khi restart ---
            if (userRepository.count() > 0) {
                log.info("Users already exist, skipping data seeding");
                return;
            }

            log.info("Seeding initial user data...");

            // 2) Create regular user account
            User regularUser = User.builder()
                    .email("user@example.com")
                    .password(passwordEncoder.encode("user123"))
                    .fullName("Regular User")
                    .roles(List.of("ROLE_USER"))
                    .status("ACTIVE")
                    .build();

            // 3) Create admin user account
            User adminUser = User.builder()
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("Administrator")
                    .roles(List.of("ROLE_USER", "ROLE_ADMIN"))
                    .status("ACTIVE")
                    .build();

            // 4) Save users to database
            userRepository.saveAll(List.of(regularUser, adminUser));

            log.info("Data seeding completed successfully");
            log.info("Test accounts created:");
            log.info("  Regular User: user@example.com / user123 (ROLE_USER)");
            log.info("  Admin User: admin@example.com / admin123 (ROLE_ADMIN)");
        };
    }
}