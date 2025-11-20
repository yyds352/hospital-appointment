package com.example.appointment.controller;

import com.example.appointment.common.Result;
import com.example.appointment.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class PasswordResetController {

    @Autowired
    private UserService userService;

    @PostMapping("/reset-all-passwords")
    public Result<String> resetAllPasswords(@RequestParam String newPassword) {
        try {
            userService.resetAllPasswords(newPassword);
            return Result.success("All passwords have been reset successfully");
        } catch (Exception e) {
            return Result.error("Failed to reset passwords: " + e.getMessage());
        }
    }
}