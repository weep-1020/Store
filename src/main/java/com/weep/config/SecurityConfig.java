package com.weep.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 配置类
 * <p>
 * 该类用于配置 Spring Security 的安全策略。
 * 由于项目使用自定义的认证过滤器（AuthenticationFilter），
 * 因此需要禁用 Spring Security 的默认认证机制，
 * 允许所有请求通过，由自定义过滤器处理认证逻辑。
 * </p>
 *
 * @author Store Team
 * @version 1.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 配置安全过滤链
     * <p>
     * 该配置会：
     * 1. 禁用 CSRF 保护（因为使用 Token 认证）
     * 2. 允许所有请求通过（由自定义过滤器处理认证）
     * 3. 禁用 HTTP Basic 认证
     * 4. 禁用表单登录
     * </p>
     * @param http HttpSecurity 对象，用于配置安全规则
     * @return 配置好的 SecurityFilterChain 对象
     * @throws Exception 配置过程中可能发生的异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF 保护（API 项目通常不需要）
            .csrf(AbstractHttpConfigurer::disable)
            
            // 配置授权规则：允许所有请求
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            
            // 禁用 HTTP Basic 认证
            .httpBasic(AbstractHttpConfigurer::disable)
            
            // 禁用表单登录
            .formLogin(AbstractHttpConfigurer::disable);
        
        return http.build();
    }
}
