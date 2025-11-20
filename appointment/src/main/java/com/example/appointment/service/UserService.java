package com.example.appointment.service;

import com.example.appointment.dto.UserDTO;
import com.example.appointment.dto.UserLoginDTO;
import com.example.appointment.dto.UserRegisterDTO;
import com.example.appointment.dto.UpdatePasswordDTO;
import com.example.appointment.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserDTO register(UserRegisterDTO registerDTO);
    UserDTO login(UserLoginDTO loginDTO);
    void logout();
    User getCurrentUser();
    UserDTO getUserByUsername(String username);
    UserDTO getCurrentUserDTO();
    UserDTO updateUser(Long id, UserDTO userDTO);
    UserDTO updateCurrentUser(UserDTO userDTO);
    void deleteUser(Long id);
    UserDTO getUserById(Long id);
    Page<UserDTO> getAllUsers(Pageable pageable);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    /**
     * 更新当前用户密码
     * @param updatePasswordDTO 包含旧密码和新密码的DTO
     */
    void updatePassword(UpdatePasswordDTO updatePasswordDTO);
    
    /**
     * 根据用户ID获取用户角色
     * @param userId 用户ID
     * @return 用户角色（如：ADMIN, USER等）
     */
    String getUserRoleById(Long userId);
    
    /**
     * 根据用户名获取User实体（仅限内部使用）
     * @param username 用户名
     * @return User实体
     */
    User findUserEntityByUsername(String username);

    /**
     * 重置所有用户的密码
     * @param newPassword 新密码（明文，会被BCrypt加密）
     */
    void resetAllPasswords(String newPassword);
}