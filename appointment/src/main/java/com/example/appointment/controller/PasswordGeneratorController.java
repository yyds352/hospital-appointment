package com.example.appointment.controller;

import com.example.appointment.common.Result;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@RestController
public class PasswordGeneratorController {
    
    @Resource
    private PasswordEncoder passwordEncoder;
    
    @GetMapping("/api/generate-password-hash")
    public Result<String> generatePasswordHash(@RequestParam String password) {
        String hash = passwordEncoder.encode(password);
        System.out.println("生成密码哈希: " + password + " -> " + hash);
        return Result.success(hash);
    }
}