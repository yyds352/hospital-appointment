package com.example.appointment.service.impl;

import com.example.appointment.common.enums.RoleEnum;
import com.example.appointment.dto.LoginDTO;
import com.example.appointment.dto.RegisterDTO;
import com.example.appointment.dto.UpdatePasswordDTO;
import com.example.appointment.dto.UpdateUserDTO;
import com.example.appointment.dto.DoctorInfoDTO;
import com.example.appointment.entity.Doctor;
import com.example.appointment.entity.User;
import com.example.appointment.entity.Department;
import com.example.appointment.repository.UserRepository;
import com.example.appointment.repository.DoctorRepository;
import com.example.appointment.repository.DepartmentRepository;
import com.example.appointment.service.UserService;
import com.example.appointment.utils.UserUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import com.example.appointment.dto.UserDTO;
import com.example.appointment.dto.UserLoginDTO;
import com.example.appointment.dto.UserRegisterDTO;
import com.example.appointment.enums.UserRole;
import com.example.appointment.utils.JwtUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.appointment.exception.UnauthorizedException;
import com.example.appointment.exception.BadRequestException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final HttpServletRequest request;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDTO register(UserRegisterDTO registerDTO) {
        // 验证用户名是否已存在
        if (userRepository.existsByUsername(registerDTO.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 验证邮箱是否已存在
        if (registerDTO.getEmail() != null && userRepository.existsByEmail(registerDTO.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 验证密码是否匹配
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new RuntimeException("两次输入的密码不一致");
        }

        // 验证角色是否有效
        try {
            UserRole.valueOf(registerDTO.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("无效的用户角色");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword())); // 使用BCrypt加密密码
        user.setName(registerDTO.getName());
        user.setRole(registerDTO.getRole().toUpperCase());
        user.setPhone(registerDTO.getPhone());
        user.setEmail(registerDTO.getEmail());

        User savedUser = userRepository.save(user);
        
        // 如果是医生角色，创建对应的医生记录
        if ("DOCTOR".equals(registerDTO.getRole().toUpperCase()) && registerDTO.getDoctorInfo() != null) {
            DoctorInfoDTO doctorInfo = registerDTO.getDoctorInfo();
            
            // 检查科室是否存在
            Department department = departmentRepository.findById(doctorInfo.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("科室不存在"));
            
            Doctor doctor = new Doctor();
            doctor.setUserId(savedUser.getId());
            doctor.setName(savedUser.getName());
            doctor.setTitle(doctorInfo.getTitle());
            doctor.setDepartment(department); // 设置关联的科室对象
            doctor.setSpecialty(doctorInfo.getSpecialty());
            doctor.setIntroduction(doctorInfo.getIntroduction());
            doctor.setStatus(1); // 设置默认状态为启用
            
            doctorRepository.save(doctor);
            log.info("医生用户注册成功，创建医生记录：用户ID={}, 医生姓名={}", savedUser.getId(), savedUser.getName());
        }
        
        return convertToDTO(savedUser);
    }

    @Override
    public UserDTO login(UserLoginDTO loginDTO) {
        log.info("用户登录请求: {}", loginDTO.getUsername());
        
        // 查找用户
        User user = userRepository.findByUsername(loginDTO.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        log.info("找到用户: {}, 密码长度: {}, 数据库密码: {}", user.getUsername(), user.getPassword().length(), user.getPassword());
        log.info("输入密码: {}", loginDTO.getPassword());

        // 验证密码（BCrypt加密比较）
        boolean passwordMatches = passwordEncoder.matches(loginDTO.getPassword(), user.getPassword());
        log.info("密码验证结果: {}", passwordMatches);
        
        if (!passwordMatches) {
            log.warn("用户登录失败：密码错误, username={}", loginDTO.getUsername());
            throw new RuntimeException("密码错误");
        }

        // 生成JWT token
        String token = jwtUtils.generateToken(user.getUsername());
        log.info("用户登录成功: {}, token长度: {}", user.getUsername(), token.length());

        // 返回用户信息和token
        UserDTO userDTO = convertToDTO(user);
        userDTO.setToken(token);
        return userDTO;
    }

    @Override
    public void logout() {
        // JWT不需要服务端注销，客户端删除token即可
    }

    @Override
    public User getCurrentUser() {
        // 优先从JwtAuthorizationFilter设置的属性中获取
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj instanceof Long) {
            Long userId = (Long) userIdObj;
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                return user;
            }
        }
        
        // 如果JwtAuthorizationFilter没有设置，尝试从UserUtils的ThreadLocal中获取
        Long userId = UserUtils.getCurrentUserId();
        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                return user;
            }
        }
        
        throw new RuntimeException("用户未登录");
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return convertToDTO(user);
    }

    @Override
    public UserDTO getCurrentUserDTO() {
        return convertToDTO(getCurrentUser());
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 只允许更新部分字段
        user.setUsername(userDTO.getUsername());
        user.setName(userDTO.getName());
        user.setPhone(userDTO.getPhone());
        user.setEmail(userDTO.getEmail());
        if (userDTO.getStatus() != null) {
            user.setStatus(userDTO.getStatus());
        }

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsByIdCustom(id)) {
            throw new RuntimeException("用户不存在");
        }
        userRepository.deleteById(id);
    }

    @Override
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return convertToDTO(user);
    }

    @Override
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::convertToDTO);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public UserDTO updateCurrentUser(UserDTO userDTO) {
        // 获取当前登录用户
        User currentUser = getCurrentUser();
        
        // 只允许更新部分安全字段
        currentUser.setName(userDTO.getName());
        currentUser.setPhone(userDTO.getPhone());
        currentUser.setEmail(userDTO.getEmail());
        
        User updatedUser = userRepository.save(currentUser);
        return convertToDTO(updatedUser);
    }

    @Override
    public String getUserRoleById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return user.getRole();
    }

    @Override
    public User findUserEntityByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    @Transactional
    @Override
    public void updatePassword(UpdatePasswordDTO updatePasswordDTO) {
        log.info("修改密码，开始验证");
        
        // 获取当前登录用户
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new UnauthorizedException("用户未登录");
        }
        
        // 验证旧密码（BCrypt加密比较）
        if (!passwordEncoder.matches(updatePasswordDTO.getOldPassword(), currentUser.getPassword())) {
            log.warn("用户密码修改失败：旧密码错误 userId={}", currentUser.getId());
            throw new BadRequestException("原密码错误");
        }
        
        // 更新密码（BCrypt加密）
        currentUser.setPassword(passwordEncoder.encode(updatePasswordDTO.getNewPassword()));
        userRepository.save(currentUser);
        
        log.info("用户密码修改成功 userId={}", currentUser.getId());
    }

    @Override
    @Transactional
    public void resetAllPasswords(String newPassword) {
        log.info("开始重置所有用户密码");
        
        // 获取所有用户
        List<User> allUsers = userRepository.findAll();
        
        // 使用BCrypt加密新密码
        String encodedPassword = passwordEncoder.encode(newPassword);
        
        // 更新所有用户的密码
        for (User user : allUsers) {
            user.setPassword(encodedPassword);
            user.setUpdatedAt(LocalDateTime.now());
        }
        
        // 批量保存
        userRepository.saveAll(allUsers);
        
        log.info("成功重置 {} 个用户的密码", allUsers.size());
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        dto.setRole(user.getRole());
        dto.setPhone(user.getPhone());
        dto.setEmail(user.getEmail());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setStatus(user.getStatus());
        return dto;
    }
}