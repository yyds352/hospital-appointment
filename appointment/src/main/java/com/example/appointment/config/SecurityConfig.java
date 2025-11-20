package com.example.appointment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/doctors/**").permitAll()
                .requestMatchers("/api/departments/**").permitAll()
                .requestMatchers("/api/symptoms/**").permitAll()
                .requestMatchers("/api/forum/**").permitAll()
                .requestMatchers("/api/forum/posts/**").permitAll()
                .requestMatchers("/api/forum/categories/**").permitAll()
                .requestMatchers("/api/forum/tags/**").permitAll()
                .anyRequest().permitAll()
            );
        
        return http.build();
    }
}