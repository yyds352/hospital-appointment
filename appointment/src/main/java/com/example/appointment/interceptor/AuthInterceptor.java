package com.example.appointment.interceptor;

import com.example.appointment.entity.User;
import com.example.appointment.service.UserService;
import com.example.appointment.utils.JwtUtils;
import com.example.appointment.utils.UserUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {
    private final JwtUtils jwtUtils;
    private final UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行登录和注册请求
        if (request.getRequestURI().startsWith("/api/auth/") || 
            request.getRequestURI().startsWith("/api/public/")) {
            return true;
        }

        // 获取token
        String token = getTokenFromRequest(request);
        if (token == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("未登录");
            return false;
        }

        // 验证token
        if (!jwtUtils.validateToken(token)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("token无效或已过期");
            return false;
        }

        // 获取用户信息
        String username = jwtUtils.getUsernameFromToken(token);
        User user = userService.findUserEntityByUsername(username);
        if (user == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("用户不存在");
            return false;
        }

        // 将用户信息存入request
        request.setAttribute("currentUser", user);
        
        // 同时设置到ThreadLocal中，供UserUtils使用
        UserUtils.setCurrentUserId(user.getId());
        return true;
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}