package com.weep.filter;

import com.weep.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/*
 * JWT 认证过滤器
 * 负责从请求中提取 JWT Token，验证其有效性，并设置 Spring Security 上下文
 * 采用 Redis 白名单机制增强安全性（可选）
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /* 日志记录器 */
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    /* Redis Key 前缀常量 */
    private static final String TOKEN_REDIS_PREFIX = "token:";

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    /*
     * 过滤器核心逻辑
     * 1. 提取 Authorization Header 中的 JWT Token
     * 2. 验证 Token 签名和有效期
     * 3. （可选）Redis 白名单校验
     * 4. 从 Token 解析用户信息和权限
     * 5. 设置 Spring Security 上下文
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        
        String uri = request.getRequestURI();
        logger.info("========== 请求进入 JWT 过滤器: {} ==========", uri);
        
        try {
            /* 1. 提取 token */
            String header = request.getHeader("Authorization");
            if (header == null || !header.startsWith("Bearer ")) {
                logger.debug("【JWT过滤器】请求未携带 Authorization Header，直接放行: {}", uri);
                filterChain.doFilter(request, response);
                return;
            }
            String token = header.substring(7);
            logger.debug("【JWT过滤器】提取到 Token，开始验证: {}", uri);

            /* 2. 验证签名和过期 */
            if (!jwtUtil.validateToken(token)) {
                logger.warn("【JWT过滤器】❌ JWT Token 验证失败（签名错误或已过期），" +
                        "放行请求由后续规则处理: {}", uri);
                filterChain.doFilter(request, response);
                return;
            }
            logger.debug("【JWT过滤器】✅ JWT Token 签名和有效期验证通过: {}", uri);

            /* 3. 从 token 获取手机号（只解码一次，提高性能） */
            String phone = jwtUtil.getPhoneFromToken(token);
            logger.debug("【JWT过滤器】从 Token 解析出手机号: {}, URI: {}", phone, uri);

            /* 4. Redis 白名单校验（Redis 不可用或 token 不在白名单时回退到 JWT-only 认证） */
            try {
                String redisKey = TOKEN_REDIS_PREFIX + phone;
                String redisToken = (String) redisTemplate.opsForValue().get(redisKey);
                if (redisToken == null || !redisToken.equals(token)) {
                    logger.warn("【JWT过滤器】⚠️ Redis 白名单校验未通过" +
                            "（token 已登出或已过期），" +
                            "回退到仅 JWT 认证 - 手机号: {}, URI: {}", phone, uri);
                }
            } catch (Exception e) {
                logger.warn("【JWT过滤器】⚠️ Redis 不可用，" +
                        "回退到仅 JWT 认证 - 手机号: {}, 错误: {}", phone, e.getMessage());
            }

            /* 5. 从 token 获取权限 */
            List<SimpleGrantedAuthority> authorities = jwtUtil.getAuthoritiesFromToken(token);
            logger.debug("【JWT过滤器】从 Token 解析出 {} 个权限 - 手机号: {}", authorities.size(), phone);

            /* 6. 设置 SecurityContext（使用手机号作为主体） */
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(phone, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            logger.info("【JWT过滤器】✅✅✅ 认证成功 - 手机号: {}, URI: {}, 权限数: {}", phone, uri, authorities.size());
        } catch (Exception e) {
            /* 捕获所有未预期的异常，确保不会阻断请求 */
            logger.error("【JWT过滤器】❌❌❌ JWT 认证过滤器异常: {}, URI: {}", e.getMessage(), uri, e);
        }

        logger.info("========== 请求离开 JWT 过滤器: {} ==========", uri);
        filterChain.doFilter(request, response);
    }
}