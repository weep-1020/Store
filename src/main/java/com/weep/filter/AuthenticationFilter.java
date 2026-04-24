package com.weep.filter;

import com.weep.util.JwtUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 认证过滤器
 * <p>
 * 该过滤器用于对受保护的 API 接口进行身份验证，主要功能包括：
 * - 检查请求是否包含有效的 JWT Token（Authorization Header）
 * - 对公开路径放行，无需认证
 * - 对未提供令牌或令牌无效的请求返回相应的错误响应
 * </p>
 * <p>
 * 注意：具体的角色和权限检查由 PermissionInterceptor 处理
 * </p>
 * <p>
 * 支持的认证方式：Bearer JWT Token
 * 公开路径：/hello, /api/auth/login, /api/auth/register, /api/test/public
 * 受保护路径：/api/*（除公开接口外）, /query
 * </p>
 *
 * @author Store Team
 * @version 3.0
 */
@Component
public class AuthenticationFilter implements Filter {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    /**
     * JWT 工具类
     */
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 公开路径列表
     * 访问这些路径的请求不需要进行身份验证
     */
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/hello",
            "/api/auth/login",
            "/api/auth/register",
            "/api/test/public"
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
        logger.info("AuthenticationFilter 初始化完成");
    }

    /**
     * 过滤器的核心处理方法
     * <p>
     * 该方法会对每个请求执行以下操作：
     * 1. 检查请求路径是否为公开路径，如果是则直接放行
     * 2. 从请求头中获取 Authorization 令牌
     * 3. 如果未提供令牌，返回 401 未授权错误
     * 4. 如果令牌无效，返回 403 禁止访问错误
     * 5. 如果令牌有效，继续执行过滤器链
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
        String method = httpRequest.getMethod();
        
        if (isPublicPath(uri)) {
            chain.doFilter(request, response);
            return;
        }
        
        String token = httpRequest.getHeader("Authorization");
        
        if (token == null || token.isEmpty()) {
            logger.warn("未授权的访问 | 方法: {} | URI: {}", method, uri);
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.getWriter().write("{\"code\":401,\"message\":\"未提供认证令牌\"}");
            return;
        }
        
        if (!validateToken(token)) {
            logger.warn("无效的认证令牌 | 方法: {} | URI: {}", method, uri);
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.getWriter().write("{\"code\":403,\"message\":\"认证令牌无效\"}");
            return;
        }
        
        logger.debug("认证成功 | 方法: {} | URI: {}", method, uri);
        chain.doFilter(request, response);
    }

    /**
     * 过滤器销毁方法
     * <p>
     * 在过滤器被容器移除时调用，用于释放资源和执行清理操作
     * </p>
     */
    @Override
    public void destroy() {
        logger.info("AuthenticationFilter 销毁");
    }

    /**
     * 判断请求路径是否为公开路径
     * <p>
     * 公开路径不需要进行身份验证，可以直接访问
     * </p>
     *
     * @param uri 请求的 URI 路径
     * @return 如果路径是公开路径返回 true，否则返回 false
     */
    private boolean isPublicPath(String uri) {
        return PUBLIC_PATHS.stream().anyMatch(uri::contains);
    }

    /**
     * 验证 JWT Token 的有效性
     * <p>
     * 该方法会执行以下验证步骤：
     * 1. 如果令牌以 "Bearer " 开头，则去除前缀
     * 2. 使用 JwtUtil 验证 Token 的签名和有效期
     * </p>
     *
     * @param token 待验证的 JWT Token 字符串
     * @return 如果令牌有效返回 true，否则返回 false
     */
    private boolean validateToken(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.validateToken(token);
    }
}
