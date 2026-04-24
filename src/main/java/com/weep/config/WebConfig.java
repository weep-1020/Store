package com.weep.config;

import com.weep.interceptor.PermissionInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类
 * <p>
 * 用于配置拦截器、资源处理器等 Web 相关配置
 * </p>
 *
 * @author Store Team
 * @version 1.0
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private PermissionInterceptor permissionInterceptor;

    /**
     * 添加拦截器
     * <p>
     * 注册权限拦截器，对所有 /api/* 路径进行权限检查
     * 排除登录、注册和公开接口
     * </p>
     *
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/test/public",
                        "/api/auth/login",
                        "/api/auth/register"
                );
    }
}
