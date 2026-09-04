package com.tiffin.system.config;

import com.tiffin.system.entity.*;
import com.tiffin.system.entity.enums.*;
import com.tiffin.system.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            log.info("Database already initialized.");
            return;
        }

        log.info("Initializing system administrator account...");

        // Create Sole Primary Admin Account
        userRepository.save(User.builder()
                .email("shivamstm01@gmail.com")
                .password(passwordEncoder.encode("Newrise@123"))
                .fullName("Shivam Admin")
                .phone("+91 9876543210")
                .department("Administration")
                .role(RoleType.ROLE_ADMIN)
                .status(UserStatus.ACTIVE)
                .build());

        log.info("==========================================================");
        log.info("Admin created successfully: shivamstm01@gmail.com");
        log.info("Ready for Admin to add custom Dishes and Combos!");
        log.info("==========================================================");
    }
}