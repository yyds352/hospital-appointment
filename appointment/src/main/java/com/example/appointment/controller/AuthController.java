package com.example.appointment.controller;

import com.example.appointment.common.Result;
import com.example.appointment.dto.UserDTO;
import com.example.appointment.dto.UserLoginDTO;
import com.example.appointment.dto.UserRegisterDTO;
import com.example.appointment.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/register")
    public Result<UserDTO> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        // 如果是医生角色注册，验证医生信息是否完整
        if ("DOCTOR".equals(registerDTO.getRole().toUpperCase())) {
            if (registerDTO.getDoctorInfo() == null) {
                throw new RuntimeException("医生注册时必须提供医生信息");
            }
            // 验证医生信息的必填字段
            if (registerDTO.getDoctorInfo().getTitle() == null || registerDTO.getDoctorInfo().getTitle().trim().isEmpty()) {
                throw new RuntimeException("医生职称不能为空");
            }
            if (registerDTO.getDoctorInfo().getDepartmentId() == null) {
                throw new RuntimeException("医生科室不能为空");
            }
            if (registerDTO.getDoctorInfo().getSpecialty() == null || registerDTO.getDoctorInfo().getSpecialty().trim().isEmpty()) {
                throw new RuntimeException("医生专业特长不能为空");
            }
        }
        
        return Result.success(userService.register(registerDTO));
    }

    @PostMapping("/login")
    public Result<UserDTO> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        return Result.success(userService.login(loginDTO));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        userService.logout();
        return Result.success();
    }

    @GetMapping("/current-user")
    public Result<UserDTO> getCurrentUser() {
        return Result.success(userService.getCurrentUserDTO());
    }
}