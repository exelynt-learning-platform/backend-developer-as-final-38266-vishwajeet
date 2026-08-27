package com.rms.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.rms.Enums.Role;
import com.rms.Repository.UserRepository;
import com.rms.entity.User;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner createUsers(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            // CREATE ADMIN
            if (userRepository.findByUsername("admin").isEmpty()) {

                User admin = new User();

                admin.setUsername("admin");
                admin.setEmail("admin@rms.com");

                admin.setPassword(
                        passwordEncoder.encode("admin123"));

                admin.setRole(Role.ADMIN);

                userRepository.save(admin);
            }

            // CREATE USER
            if (userRepository.findByUsername("user").isEmpty()) {

                User user = new User();

                user.setUsername("user");
                user.setEmail("user@rms.com");

                user.setPassword(
                        passwordEncoder.encode("user123"));

                user.setRole(Role.USER);

                userRepository.save(user);
            }
        };
    }
}