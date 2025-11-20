package com.example.appointment.filter;

import com.example.appointment.entity.User;
import com.example.appointment.repository.UserRepository;
import com.example.appointment.utils.JwtUtils;
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
import java.util.Optional;

/**
 * JWT认证过滤器，在AuthenticationFilter之前执行
 * 用于解析JWT token并设置用户信息到请求属性中
 */
@Component
@Order(1) // 确保这个过滤器在AuthenticationFilter之前执行
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {
    
    private static final Logger log = LoggerFactory.getLogger(JwtAuthorizationFilter.class);

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        // 获取Authorization头
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            try {
                // 验证token
                if (jwtUtils.validateToken(token)) {
                    // 从token中获取用户名
                    String username = jwtUtils.getUsernameFromToken(token);
                    
                    // 从数据库获取用户信息
                    Optional<User> userOptional = userRepository.findByUsername(username);
                    
                    if (userOptional.isPresent()) {
                        User user = userOptional.get();
                        // 将用户信息设置到请求属性中
                        request.setAttribute("userId", user.getId());
                        request.setAttribute("username", user.getUsername());
                        request.setAttribute("userRole", user.getRole());
                        
                        // 同时设置到ThreadLocal中，供UserUtils使用
                        UserUtils.setCurrentUserId(user.getId());
                    }
                }
            } catch (Exception e) {
                log.error("JWT token解析失败", e);
                // 如果token解析失败，不设置用户信息，继续执行过滤器链
            }
        }
        
        // 继续执行过滤器链
        filterChain.doFilter(request, response);
    }
}