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
    public CommandLineRunner userSeeder(UserRepository repo, PasswordEncoder encoder) {
        return args -> {
            seedUser(repo, encoder, adminEmail, adminPassword, Role.ADMIN);
            seedUser(repo, encoder, "doctor@gmail.com", "doctor@123", Role.DOCTOR);
            seedUser(repo, encoder, "patient@gmail.com", "patient@123", Role.PATIENT);
        };
    }

    private void seedUser(UserRepository repo, PasswordEncoder encoder,
                          String email, String password, Role role) {
        if (repo.existsByEmail(email)) {
            log.info("{} user '{}' already present, skipping seed.", role, email);
            return;
        }
        repo.save(User.builder()
                .email(email)
                .password(encoder.encode(password))
                .role(role)
                .build());
        log.info("Seeded default {} user '{}'.", role, email);
    }
}
