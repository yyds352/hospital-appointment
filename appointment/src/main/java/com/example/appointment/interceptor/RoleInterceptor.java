package com.example.appointment.interceptor;

import com.example.appointment.annotation.RequireRole;
import com.example.appointment.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Component
public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        
        if (requireRole == null) {
            return true;
        }

        User user = (User) request.getAttribute("currentUser");
        if (user == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("未登录");
            return false;
        }

        if (!Arrays.asList(requireRole.value()).contains(user.getRole())) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write("无权限访问");
            return false;
        }

        return true;
    }
} 