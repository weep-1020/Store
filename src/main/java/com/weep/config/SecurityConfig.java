package com.weep.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weep.filter.JwtAuthenticationFilter;
import com.weep.security.CustomAuthenticationProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
@EnableMethodSecurity(prePostEnabled = true)  // 开启方法级权限控制
@RequiredArgsConstructor
public class SecurityConfig {

    /* 日志记录器 */
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtAuthenticationFilter jwtAuthenticationFilter;   // JWT 校验过滤器
    
    /*
     * 创建密码编码器 Bean
     * 使用 BCrypt 算法进行密码加密和验证
     *
     * @return PasswordEncoder 实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
     * 创建认证管理器 Bean
     * 使用自定义的 AuthenticationProvider 进行认证
     *
     * @param customAuthenticationProvider 自定义认证提供者（通过方法参数注入，避免循环依赖）
     * @return AuthenticationManager 实例
     */
    @Bean
    public AuthenticationManager authenticationManager(CustomAuthenticationProvider customAuthenticationProvider) {
        return new ProviderManager(customAuthenticationProvider);
    }
    /*
     * 配置安全过滤链
     * 该配置会：
     * 1. 禁用 CSRF 保护（因为使用 Token 认证）
     * 2. 允许所有请求通过（由自定义过滤器处理认证）
     * 3. 禁用 HTTP Basic 认证
     * 4. 禁用表单登录
     *
     * @param http HttpSecurity 对象，用于配置安全规则
     * @return 配置好的 SecurityFilterChain 对象
     * @throws Exception 配置过程中可能发生的异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("请求进入安全过滤连");
        http
            // 禁用 CSRF 保护（API 项目通常不需要）
            .csrf(AbstractHttpConfigurer::disable)
            
            // 配置 CORS 跨域支持
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 配置授权规则
            .authorizeHttpRequests(auth -> auth
                    /* 公开路径：无需认证即可访问 */
                    .requestMatchers(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/validate",
                        "/api/auth/testO",
                        "/api/test/*",
                        "/api/public/**",
                        "/api/categories/**",
                        "/api/carousels/**"
                    ).permitAll()
                    /* 商品查询接口公开访问（GET 请求） */
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/products/**").permitAll()
                    /* 评价查询接口公开访问（GET 请求） */
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/reviews/product/**").permitAll()

                    /* 管理员路径：需要 SUPER_ADMIN 或 MERCHANT 角色 */
                    .requestMatchers("/api/admin/**").hasAnyRole("SUPER_ADMIN", "MERCHANT")
                    
                    /* 商家路径：需要 SUPER_ADMIN 或 MERCHANT 角色 */
                    .requestMatchers("/api/merchant/**").hasAnyRole("SUPER_ADMIN", "MERCHANT")
                    .requestMatchers("/api/user/**").hasRole("USER")
                    
                    /* 其他所有请求需要认证 */
                    .anyRequest().authenticated()
            )
                // 4. 添加 JWT 校验过滤器（在 UsernamePasswordAuthenticationFilter 之前）
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // 5. 异常处理
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )
                // 6. 设置无状态 Session 管理 (Lambda DSL)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
            
            // 禁用 HTTP Basic 认证
            .httpBasic(AbstractHttpConfigurer::disable)
            
            // 禁用表单登录
            .formLogin(AbstractHttpConfigurer::disable);
        
        return http.build();
    }

    /*
     * 创建认证入口点 Bean
     * 用于处理未认证用户访问受保护资源时的响应
     *
     * @return AuthenticationEntryPoint 实例，返回 JSON 格式的错误信息
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            String uri = request.getRequestURI();
            logger.warn("【安全配置】 未认证访问被拦截 - URI: {}, IP: {}", uri, request.getRemoteAddr());
            
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 401);
            result.put("message", "未认证，请先登录");
            result.put("path", uri);
            
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(response.getWriter(), result);
        };
    }

    /*
     * 创建访问拒绝处理器 Bean
     * 用于处理已认证但权限不足的用户访问受保护资源时的响应
     *
     * @return AccessDeniedHandler 实例，返回 JSON 格式的错误信息
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            String uri = request.getRequestURI();
            logger.warn("【安全配置】 权限不足被拦截 - URI: {}, IP: {}, 原因: {}", uri, request.getRemoteAddr(), accessDeniedException.getMessage());
            
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 403);
            result.put("message", "权限不足，无法访问该资源");
            result.put("path", uri);
            
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(response.getWriter(), result);
        };
    }

    /*
     * 配置 CORS 跨域策略
     * 允许前端应用跨域访问后端 API
     *
     * @return CorsConfigurationSource CORS 配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        /* 允许的源地址（前端地址） */
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",   // React/Vue 开发服务器
                "http://localhost:5173",   // Vite 默认端口
                "http://localhost:8080"    // 其他前端服务
        ));
        
        /* 允许的 HTTP 方法 */
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        
        /* 允许的请求头（不能使用 * 配合 credentials） */
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin"
        ));
        
        /* 是否允许携带凭证（Cookie、Authorization Header 等） */
        configuration.setAllowCredentials(true);
        
        /* 预检请求缓存时间（秒），避免频繁发送 OPTIONS 请求 */
        configuration.setMaxAge(3600L);
        
        /* 暴露的响应头（允许前端访问的响应头） */
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With"
        ));
        
        /* 应用到所有路径 */
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
