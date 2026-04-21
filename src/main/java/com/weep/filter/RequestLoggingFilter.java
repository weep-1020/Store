package com.weep.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 请求日志过滤器
 * <p>
 * 该过滤器用于记录所有 HTTP 请求的详细信息，包括：
 * - 请求方法和 URI
 * - 客户端 IP 地址
 * - 响应状态码
 * - 请求处理耗时
 * - 异常信息
 * </p>
 * <p>
 * 过滤器会在请求开始时记录日志，在请求结束时记录响应状态和耗时，
 * 如果请求过程中发生异常，也会记录异常信息。
 * </p>
 *
 * @author Store Team
 * @version 1.0
 */
@Component
public class RequestLoggingFilter implements Filter {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    /**
     * 需要排除的路径列表
     * 这些路径的请求不会被记录日志
     */
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/favicon.ico",
            "/error"
    );

    /**
     * 过滤器初始化方法
     * <p>
     * 在过滤器创建时调用，用于执行初始化操作
     * </p>
     *
     * @param filterConfig 过滤器配置对象，包含初始化参数
     * @throws ServletException 当初始化过程发生错误时抛出
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("RequestLoggingFilter 初始化完成");
    }

    /**
     * 过滤器的核心处理方法
     * <p>
     * 该方法会对每个请求执行以下操作：
     * 1. 检查请求路径是否在排除列表中，如果是则直接放行
     * 2. 记录请求开始时间、方法、URI 和客户端 IP
     * 3. 执行过滤器链中的下一个过滤器或目标资源
     * 4. 记录请求结束时间、响应状态码和处理耗时
     * 5. 如果发生异常，记录异常信息并重新抛出
     * </p>
     *
     * @param request  Servlet 请求对象，包含客户端请求信息
     * @param response Servlet 响应对象，用于向客户端发送响应
     * @param chain    过滤器链对象，用于调用下一个过滤器或目标资源
     * @throws IOException      当发生 I/O 错误时抛出
     * @throws ServletException 当发生 Servlet 相关错误时抛出
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String uri = httpRequest.getRequestURI();
        
        if (shouldExclude(uri)) {
            chain.doFilter(request, response);
            return;
        }
        
        long startTime = System.currentTimeMillis();
        
        logger.info("请求开始 | 方法: {} | URI: {} | IP: {}",
                httpRequest.getMethod(),
                uri,
                getClientIp(httpRequest));
        
        try {
            chain.doFilter(request, response);
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("请求结束 | 方法: {} | URI: {} | 状态码: {} | 耗时: {}ms",
                    httpRequest.getMethod(),
                    uri,
                    httpResponse.getStatus(),
                    duration);
                    
        } catch (Exception e) {
            logger.error("请求异常 | 方法: {} | URI: {} | 异常: {}",
                    httpRequest.getMethod(),
                    uri,
                    e.getMessage());
            throw e;
        }
    }

    /**
     * 过滤器销毁方法
     * <p>
     * 在过滤器被容器移除时调用，用于释放资源和执行清理操作
     * </p>
     */
    @Override
    public void destroy() {
        logger.info("RequestLoggingFilter 销毁");
    }

    /**
     * 判断请求路径是否应该被排除在日志记录之外
     *
     * @param uri 请求的 URI 路径
     * @return 如果路径在排除列表中返回 true，否则返回 false
     */
    private boolean shouldExclude(String uri) {
        return EXCLUDED_PATHS.stream().anyMatch(uri::endsWith);
    }

    /**
     * 获取客户端的真实 IP 地址
     * <p>
     * 该方法会依次尝试从以下来源获取 IP 地址：
     * 1. X-Forwarded-For 请求头（适用于经过代理或负载均衡的场景）
     * 2. X-Real-IP 请求头（某些反向代理使用）
     * 3. 远程地址（直连场景）
     * </p>
     *
     * @param request HTTP 请求对象
     * @return 客户端的 IP 地址字符串
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
