package com.example.knowledgesphere.util;

import com.example.knowledgesphere.entity.Role;
import com.example.knowledgesphere.entity.User;
import com.example.knowledgesphere.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (userRepository.existsByRole(Role.ROLE_ADMIN)) {
            return;
        }

        User admin = User.builder()
                .fullName("System Administrator")
                .email("admin@knowledgesphere.com")
                .password(passwordEncoder.encode("Admin@123"))
                .role(Role.ROLE_ADMIN)
                .enabled(true)
                .build();

        userRepository.save(admin);

        System.out.println("Admin user created.");
    }
}