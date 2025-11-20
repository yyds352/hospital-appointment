package com.example.appointment.controller;

import com.example.appointment.common.Result;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {
    
    private static final Logger log = LoggerFactory.getLogger(TestController.class);
    
    private final PasswordEncoder passwordEncoder;
    
    @GetMapping("/bcrypt/{rawPassword}/{encodedPassword}")
    public Result<Boolean> testBCrypt(@PathVariable String rawPassword, @PathVariable String encodedPassword) {
        log.info("测试BCrypt: rawPassword={}, encodedPassword={}", rawPassword, encodedPassword);
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
        log.info("BCrypt匹配结果: {}", matches);
        return Result.success(matches);
    }
    
    @GetMapping("/bcrypt/generate/{rawPassword}")
    public Result<String> generateBCrypt(@PathVariable String rawPassword) {
        String encoded = passwordEncoder.encode(rawPassword);
        log.info("生成BCrypt: rawPassword={}, encoded={}", rawPassword, encoded);
        return Result.success(encoded);
    }
}