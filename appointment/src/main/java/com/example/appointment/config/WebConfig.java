package com.example.appointment.config;

import com.example.appointment.interceptor.AuthInterceptor;
import com.example.appointment.interceptor.RoleInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final AuthInterceptor authInterceptor;
    private final RoleInterceptor roleInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 认证拦截器
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                    "/api/auth/register",  // 注册接口不需要认证
                    "/api/auth/login",     // 登录接口不需要认证
                    "/api/users/login",    // 用户登录接口不需要认证
                    "/api/users/register", // 用户注册接口不需要认证
                    "/api/public/**",
                    "/api/post-categories/**",
                    "/api/posts/**",
                    "/api/forum/posts",  // 获取帖子列表允许未登录访问
                    "/api/forum/posts/{id}",  // 获取帖子详情允许未登录访问
                    "/api/forum/posts/search",  // 搜索帖子允许未登录访问
                    "/api/forum/posts/latest",  // 获取最新帖子允许未登录访问
                    "/api/forum/posts/{id}/like",  // 点赞帖子允许未登录访问
                    "/api/forum/posts",  // 创建帖子允许未登录访问
                    "/api/forum/posts/{postId}/comments",  // 获取/创建评论允许未登录访问
                    "/api/forum/posts/{postId}/comments/{commentId}/like",  // 点赞评论允许未登录访问
                    "/api/forum/categories",  // 获取分类允许未登录访问
                    "/api/forum/stats",  // 获取统计信息允许未登录访问
                    "/api/forum/latest-comments",  // 获取最新评论允许未登录访问
                    "/api/health-posts/**",
                    "/api/doctor/**",
                    "/api/departments/**"
                )
                .order(1);
        
        // 角色拦截器
        registry.addInterceptor(roleInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                    "/api/auth/**",
                    "/api/users/login",
                    "/api/users/register",
                    "/api/public/**",
                    "/api/post-categories/**",
                    "/api/posts/**",
                    "/api/forum/posts",  // 获取帖子列表允许未登录访问
                    "/api/forum/posts/{id}",  // 获取帖子详情允许未登录访问
                    "/api/forum/posts/search",  // 搜索帖子允许未登录访问
                    "/api/forum/posts/latest",  // 获取最新帖子允许未登录访问
                    "/api/forum/posts/{postId}/comments",  // 获取评论列表允许未登录访问
                    "/api/forum/categories",  // 获取分类允许未登录访问
                    "/api/forum/stats",  // 获取统计信息允许未登录访问
                    "/api/forum/latest-comments",  // 获取最新评论允许未登录访问
                    "/api/health-posts/**",
                    "/api/doctor/**",
                    "/api/departments/**"
                )
                .order(2);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type", "X-Requested-With")
                .exposedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}