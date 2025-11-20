package com.example.appointment.controller;

import com.example.appointment.annotation.RequireRole;
import com.example.appointment.common.Result;
import com.example.appointment.dto.*;
import com.example.appointment.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/login")
    public Result<UserDTO> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        return Result.success(userService.login(loginDTO));
    }

    @PostMapping("/register")
    public Result<UserDTO> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        return Result.success(userService.register(registerDTO));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        userService.logout();
        return Result.success();
    }

    @GetMapping("/current")
    public Result<UserDTO> getCurrentUser() {
        return Result.success(userService.getCurrentUserDTO());
    }

    @PutMapping("/current")
    public Result<UserDTO> updateCurrentUser(@Valid @RequestBody UserDTO userDTO) {
        return Result.success(userService.updateCurrentUser(userDTO));
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody UpdatePasswordDTO updatePasswordDTO) {
        userService.updatePassword(updatePasswordDTO);
        return Result.success();
    }

    @PutMapping("/{id}")
    @RequireRole({"ADMIN"})
    public Result<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO userDTO) {
        return Result.success(userService.updateUser(id, userDTO));
    }

    @DeleteMapping("/{id}")
    @RequireRole({"ADMIN"})
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @RequireRole({"ADMIN"})
    public Result<UserDTO> getUserById(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @GetMapping
    @RequireRole({"ADMIN"})
    public Result<Page<UserDTO>> getAllUsers(Pageable pageable) {
        return Result.success(userService.getAllUsers(pageable));
    }

    @PostMapping
    @RequireRole({"ADMIN"})
    public Result<UserDTO> addUser(@Valid @RequestBody UserRegisterDTO registerDTO) {
        return Result.success(userService.register(registerDTO));
    }

    @GetMapping("/by-username/{username}")
    @RequireRole({"ADMIN"})
    public Result<UserDTO> getUserByUsername(@PathVariable String username) {
        return Result.success(userService.getUserByUsername(username));
    }

    @PutMapping("/{id}/status")
    @RequireRole({"ADMIN"})
    public Result<UserDTO> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
        Integer status = request.get("status");
        if (status == null || (status != 0 && status != 1)) {
            return Result.error("状态值只能是0或1");
        }
        
        // 获取现有用户信息
        UserDTO existingUser = userService.getUserById(id);
        existingUser.setStatus(status);
        return Result.success(userService.updateUser(id, existingUser));
    }
}