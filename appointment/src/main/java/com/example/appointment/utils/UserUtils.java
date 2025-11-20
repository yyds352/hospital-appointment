package com.example.appointment.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 用户工具类
 */
@Component
public class UserUtils {

    private static final ThreadLocal<Long> currentUser = new ThreadLocal<>();
    private static JwtUtils jwtUtils;

    @Autowired
    public void setJwtUtils(JwtUtils jwtUtils) {
        UserUtils.jwtUtils = jwtUtils;
    }

    /**
     * 获取当前登录用户的ID
     * @return 用户ID，如果未登录则返回null
     */
    public static Long getCurrentUserId() {
        // 优先从ThreadLocal获取
        Long userId = currentUser.get();
        if (userId != null) {
            return userId;
        }
        
        // 如果ThreadLocal中没有，尝试从当前请求的属性中获取
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                Object userIdObj = request.getAttribute("userId");
                if (userIdObj instanceof Long) {
                    return (Long) userIdObj;
                }
            }
        } catch (Exception e) {
            // 如果获取请求上下文失败，继续执行
        }
        
        return null;
    }

    /**
     * 设置当前用户ID（在拦截器或过滤器中使用）
     * @param userId 用户ID
     */
    public static void setCurrentUserId(Long userId) {
        currentUser.set(userId);
    }

    /**
     * 清除当前用户ID
     */
    public static void clearCurrentUserId() {
        currentUser.remove();
    }
    
    /**
     * 从请求中解析用户ID
     * @param request HTTP请求
     * @return 用户ID，如果无法解析则返回null
     */
    public static Long getUserIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                // 使用JwtUtils验证和解析token
                if (jwtUtils != null && jwtUtils.validateToken(token)) {
                    String username = jwtUtils.getUsernameFromToken(token);
                    // 通过用户名查找用户ID
                    // 这里需要从数据库或缓存中获取用户ID
                    // 临时方案：从请求属性中获取，在AuthorizationFilter中设置
                    Object userId = request.getAttribute("userId");
                    if (userId instanceof Long) {
                        return (Long) userId;
                    }
                }
            } catch (Exception e) {
                // 解析失败，记录异常但继续执行
                System.err.println("Token解析失败: " + e.getMessage());
            }
        }
        return null;
    }
}