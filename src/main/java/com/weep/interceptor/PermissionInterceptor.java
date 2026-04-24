package com.weep.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weep.annotation.RequirePermission;
import com.weep.annotation.RequireRole;
import com.weep.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 权限拦截器
 * <p>
 * 该拦截器用于检查用户是否具有访问特定接口所需的角色和权限。
 * 通过解析 @RequireRole 和 @RequirePermission 注解来实现方法级别的权限控制。
 * </p>
 *
 * @author Store Team
 * @version 1.0
 */
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(PermissionInterceptor.class);

    @Autowired
    private JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 在请求处理之前进行权限检查
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @param handler  处理器（通常是 Controller 方法）
     * @return 如果权限验证通过返回 true，否则返回 false
     * @throws Exception 处理异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只处理方法级别的请求
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // 检查是否有角色或权限注解
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        RequirePermission requirePermission = handlerMethod.getMethodAnnotation(RequirePermission.class);

        // 如果没有注解，直接放行
        if (requireRole == null && requirePermission == null) {
            return true;
        }

        // 从请求头获取 Token
        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isEmpty()) {
            sendErrorResponse(response, 401, "未提供认证令牌");
            return false;
        }

        // 提取 Token（去除 Bearer 前缀）
        String token = authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;

        // 验证 Token 有效性
        if (!jwtUtil.validateToken(token)) {
            sendErrorResponse(response, 403, "认证令牌无效");
            return false;
        }

        // 获取用户的角色和权限
        List<String> userRoles = jwtUtil.getRolesFromToken(token);
        List<String> userPermissions = jwtUtil.getPermissionsFromToken(token);

        logger.debug("用户角色: {}, 用户权限: {}", userRoles, userPermissions);

        // 检查角色权限
        if (requireRole != null && !checkRole(userRoles, requireRole)) {
            sendErrorResponse(response, 403, "没有足够的角色权限访问该资源");
            return false;
        }

        // 检查功能权限
        if (requirePermission != null && !checkPermission(userPermissions, requirePermission)) {
            sendErrorResponse(response, 403, "没有足够的功能权限访问该资源");
            return false;
        }

        logger.info("权限验证通过 - URI: {}", request.getRequestURI());
        return true;
    }

    /**
     * 检查用户是否具有所需角色
     *
     * @param userRoles   用户的角色列表
     * @param requireRole 需要的角色注解
     * @return 如果具有所需角色返回 true，否则返回 false
     */
    private boolean checkRole(List<String> userRoles, RequireRole requireRole) {
        String[] requiredRoles = requireRole.value();
        RequireRole.Logical logical = requireRole.logical();

        if (logical == RequireRole.Logical.AND) {
            // 需要拥有所有角色
            for (String role : requiredRoles) {
                if (!userRoles.contains(role)) {
                    logger.warn("缺少必需角色: {}", role);
                    return false;
                }
            }
            return true;
        } else {
            // 只需要拥有其中一个角色
            for (String role : requiredRoles) {
                if (userRoles.contains(role)) {
                    return true;
                }
            }
            logger.warn("缺少任一必需角色: {}", String.join(", ", requiredRoles));
            return false;
        }
    }

    /**
     * 检查用户是否具有所需权限
     *
     * @param userPermissions   用户的权限列表
     * @param requirePermission 需要的权限注解
     * @return 如果具有所需权限返回 true，否则返回 false
     */
    private boolean checkPermission(List<String> userPermissions, RequirePermission requirePermission) {
        String[] requiredPermissions = requirePermission.value();
        RequirePermission.Logical logical = requirePermission.logical();

        if (logical == RequirePermission.Logical.AND) {
            // 需要拥有所有权限
            for (String permission : requiredPermissions) {
                if (!userPermissions.contains(permission)) {
                    logger.warn("缺少必需权限: {}", permission);
                    return false;
                }
            }
            return true;
        } else {
            // 只需要拥有其中一个权限
            for (String permission : requiredPermissions) {
                if (userPermissions.contains(permission)) {
                    return true;
                }
            }
            logger.warn("缺少任一必需权限: {}", String.join(", ", requiredPermissions));
            return false;
        }
    }

    /**
     * 发送错误响应
     *
     * @param response HTTP 响应
     * @param code     错误码
     * @param message  错误消息
     */
    private void sendErrorResponse(HttpServletResponse response, int code, String message) {
        try {
            response.setStatus(code);
            response.setContentType("application/json;charset=UTF-8");

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", code);
            errorResponse.put("message", message);

            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        } catch (Exception e) {
            logger.error("发送错误响应失败: {}", e.getMessage());
        }
    }
}
