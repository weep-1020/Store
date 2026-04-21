package com.weep.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置类
 * <p>
 * 该类用于配置应用程序的跨域资源共享（CORS）策略，允许前端应用跨域访问后端 API。
 * 主要配置包括：
 * - 允许的源域名
 * - 允许的 HTTP 方法
 * - 允许的请求头
 * - 是否允许携带凭证
 * - 预检请求的缓存时间
 * </p>
 *
 * @author Store Team
 * @version 1.0
 */
@Configuration
public class CorsConfig {

    /**
     * 配置跨域过滤器
     * <p>
     * 该 Bean 会创建一个 CorsFilter，用于处理所有跨域请求。
     * 配置说明：
     * - 允许所有源域名访问（生产环境应该指定具体域名）
     * - 允许常用的 HTTP 方法：GET, POST, PUT, DELETE, OPTIONS
     * - 允许所有请求头
     * - 允许携带认证信息（Cookie、Token 等）
     * - 预检请求缓存 3600 秒
     * </p>
     *
     * @return 配置好的 CorsFilter 对象
     */
    @Bean
    public CorsFilter corsFilter() {
        // 创建 CORS 配置对象
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许所有源域名（生产环境应该改为具体的域名，如：http://localhost:3000）
        config.addAllowedOriginPattern("*");
        
        // 允许携带认证信息（Cookie、Authorization Header 等）
        config.setAllowCredentials(true);
        
        // 允许所有请求头
        config.addAllowedHeader("*");
        
        // 允许所有 HTTP 方法
        config.addAllowedMethod("*");
        
        // 暴露响应头，让前端可以访问自定义响应头
        config.addExposedHeader("Authorization");
        config.addExposedHeader("Content-Type");
        
        // 预检请求的有效期（秒），在此期间不需要再次发送预检请求
        config.setMaxAge(3600L);
        
        // 创建基于 URL 的 CORS 配置源
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        
        // 对所有路径应用 CORS 配置
        source.registerCorsConfiguration("/**", config);
        
        // 返回 CORS 过滤器
        return new CorsFilter(source);
    }
}
