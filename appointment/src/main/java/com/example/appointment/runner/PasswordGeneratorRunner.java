package com.example.appointment.runner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordGeneratorRunner implements CommandLineRunner {
    
    private final PasswordEncoder passwordEncoder;
    
    public PasswordGeneratorRunner(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public void run(String... args) throws Exception {
        if (args.length > 0 && "generate-passwords".equals(args[0])) {
            System.out.println("=== 密码生成器 ===");
            System.out.println("密码 '123456' 的BCrypt哈希: " + passwordEncoder.encode("123456"));
            System.out.println("密码 'admin123' 的BCrypt哈希: " + passwordEncoder.encode("admin123"));
            System.out.println("密码 'testpass123' 的BCrypt哈希: " + passwordEncoder.encode("testpass123"));
            System.out.println("=== 生成完成 ===");
        }
    }
}