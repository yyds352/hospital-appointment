package com.example.appointment.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class SecurityUtils {
    public static Long getCurrentUserId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String token = request.getHeader("Authorization");
            
            // 尝试从线程上下文中获取用户ID
            Long userIdFromContext = UserUtils.getCurrentUserId();
            if (userIdFromContext != null) {
                return userIdFromContext;
            }
            
            if (token != null) {
                // 支持旧格式 Bearer_userId_xxx
                if (token.startsWith("Bearer_")) {
                    String[] parts = token.split("_");
                    if (parts.length == 3) {
                        try {
                            return Long.parseLong(parts[1]);
                        } catch (NumberFormatException e) {
                            throw new IllegalStateException("Invalid token format");
                        }
                    }
                } 
                // 支持标准JWT格式 Bearer xxx
                else if (token.startsWith("Bearer ")) {
                    // 从请求属性中获取用户ID（由JwtAuthorizationFilter设置）
                    Object userId = request.getAttribute("userId");
                    if (userId instanceof Long) {
                        return (Long) userId;
                    }
                }
            }
        }
        throw new IllegalStateException("Not authenticated");
    }
} 