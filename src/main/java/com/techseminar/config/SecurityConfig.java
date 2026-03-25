package com.techseminar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 보안 설정: 데모 목적으로 Spring Security의 보호 기능을 의도적으로 비활성화.
 * 이 설정을 운영 환경에서 사용하지 말 것.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화 (데모 목적)
            .csrf(AbstractHttpConfigurer::disable)
            // 모든 요청 허용 (인증 불필요)
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            // 기본 로그인 페이지 비활성화 (커스텀 구현)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
