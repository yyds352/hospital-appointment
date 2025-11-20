package com.example.appointment.annotation;

import com.example.appointment.exception.UnauthorizedException;
import com.example.appointment.service.UserService;
import com.example.appointment.utils.UserUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 角色验证切面
 * 用于在方法执行前验证用户角色
 */
@Aspect
@Component
public class RequireRoleAspect {

    @Autowired
    private UserService userService;

    @Before("@annotation(requireRole)")
    public void checkRole(JoinPoint joinPoint, RequireRole requireRole) {
        Long userId = UserUtils.getCurrentUserId();
        if (userId == null) {
            throw new UnauthorizedException("未登录");
        }

        List<String> allowedRoles = Arrays.asList(requireRole.value());
        String userRole = userService.getUserRoleById(userId);
        
        if (!allowedRoles.contains(userRole)) {
            throw new UnauthorizedException("没有权限执行此操作");
        }
    }
} 