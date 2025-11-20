package com.example.appointment.filter;

import com.example.appointment.utils.UserUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 自定义认证过滤器
 * 用于在不使用Spring Security的情况下解析请求中的认证信息
 */
@Component
@Order(2) // 在JwtAuthorizationFilter之后执行
@RequiredArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // 从请求属性中获取用户ID（由JwtAuthorizationFilter设置）
            Object userId = request.getAttribute("userId");
            
            // 如果请求属性中有用户ID，就将其设置到当前线程上下文中
            if (userId instanceof Long) {
                UserUtils.setCurrentUserId((Long) userId);
                log.debug("设置当前用户ID: {}", userId);
            } else {
                // 如果请求属性中没有，则尝试从请求中解析
                Long parsedUserId = UserUtils.getUserIdFromRequest(request);
                if (parsedUserId != null) {
                    UserUtils.setCurrentUserId(parsedUserId);
                    log.debug("从请求中解析用户ID: {}", parsedUserId);
                }
            }
            
            // 继续执行过滤器链
            filterChain.doFilter(request, response);
        } finally {
            // 请求结束后清除线程上下文中的用户信息，防止内存泄漏
            UserUtils.clearCurrentUserId();
        }
    }
}