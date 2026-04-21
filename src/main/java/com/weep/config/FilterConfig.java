package com.weep.config;

import com.weep.filter.AuthenticationFilter;
import com.weep.filter.RequestLoggingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 过滤器配置类
 * <p>
 * 该类用于配置和注册应用程序中的过滤器，定义过滤器的执行顺序和 URL 匹配规则。
 * 当前配置的过滤器包括：
 * - RequestLoggingFilter: 请求日志过滤器，记录所有请求的详细信息
 * - AuthenticationFilter: 认证过滤器，对受保护的 API 进行身份验证
 * </p>
 *
 * @author Store Team
 * @version 1.0
 */
@Configuration
public class FilterConfig {

    /**
     * 注册请求日志过滤器
     * <p>
     * 该过滤器会拦截所有请求（/*），并记录请求的详细信息。
     * 执行顺序为 1，表示它会在其他过滤器之前执行。
     * </p>
     *
     * @param filter 通过依赖注入获取的 RequestLoggingFilter 实例
     * @return 配置好的 FilterRegistrationBean 对象
     */
    @Bean
    public FilterRegistrationBean<RequestLoggingFilter> loggingFilterRegistration(
            RequestLoggingFilter filter) {
        
        FilterRegistrationBean<RequestLoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns("/*");
        registration.setName("requestLoggingFilter");
        registration.setOrder(1);
        
        return registration;
    }

    /**
     * 注册认证过滤器
     * <p>
     * 该过滤器会拦截 /api/* 和 /query 路径的请求，并进行身份验证。
     * 执行顺序为 2，表示它会在请求日志过滤器之后执行。
     * </p>
     *
     * @param filter 通过依赖注入获取的 AuthenticationFilter 实例
     * @return 配置好的 FilterRegistrationBean 对象
     */
    @Bean
    public FilterRegistrationBean<AuthenticationFilter> authFilterRegistration(
            AuthenticationFilter filter) {
        
        FilterRegistrationBean<AuthenticationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns("/api/*", "/query","/admin/*");
        registration.setName("authenticationFilter");
        registration.setOrder(2);
        
        return registration;
    }
}
