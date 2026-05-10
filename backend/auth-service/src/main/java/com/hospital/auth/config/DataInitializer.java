package com.hospital.auth.config;

import com.hospital.auth.entity.Role;
import com.hospital.auth.entity.User;
import com.hospital.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Bean
    public CommandLineRunner adminSeeder(UserRepository repo, PasswordEncoder encoder) {
        return args -> {
            if (repo.existsByEmail(adminEmail)) {
                log.info("Admin user '{}' already present, skipping seed.", adminEmail);
                return;
            }
            repo.save(User.builder()
                    .email(adminEmail)
                    .password(encoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .build());
            log.info("Seeded default admin user '{}'.", adminEmail);
        };
    }
}
