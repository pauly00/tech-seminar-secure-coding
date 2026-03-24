package com.techseminar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security config: Spring Security protections intentionally disabled for demo purposes.
 * DO NOT use this configuration in production.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF (demo purpose)
            .csrf(AbstractHttpConfigurer::disable)
            // Allow all requests (no auth required)
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            // Disable default login page (custom impl)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
