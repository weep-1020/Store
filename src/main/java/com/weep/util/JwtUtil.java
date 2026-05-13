package com.weep.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.weep.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * JWT 工具类
 *
 * <p>负责 JWT Token 的生成、验证和解析。
 * 使用 auth0-jwt 库实现 HMAC256 签名算法，确保 Token 的安全性。
 */
@Component
public class JwtUtil {

    /** 日志记录器 */
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    /** JWT 密钥 */
    private String secret;

    /** Token 过期时间（毫秒） */
    @Value("${jwt.expiration}")
    private Long expiration;

    /** 缓存的 Algorithm 实例，避免重复创建 */
    private Algorithm algorithm;

    /**
     * 设置密钥并初始化 Algorithm 实例
     * 
     * <p>Spring 注入 secret 后自动调用此方法完成初始化。
     *
     * @param secret JWT 密钥
     */
    @Value("${jwt.secret}")
    public void setSecretAndInitAlgorithm(String secret) {
        this.secret = secret;
        this.algorithm = Algorithm.HMAC256(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 获取 Token 过期时间
     *
     * @return 过期时间（毫秒）
     */
    public Long getExpiration() {
        return expiration;
    }

    /**
     * 生成 JWT Token（包含用户详细信息、角色和权限）
     * 
     * <p>将角色和权限列表转换为逗号分隔的字符串存储到 claims 中，
     * 使用手机号作为 Token 的主体（subject）。
     *
     * @param user 用户对象
     * @param roles 角色列表
     * @param permissions 权限列表
     * @return JWT Token 字符串
     */
    public String generateTokenWithUserDetails(User user, List<String> roles, List<String> permissions) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        /* 将角色列表转换为逗号分隔的字符串 */
        String rolesStr = roles != null && !roles.isEmpty()
                ? String.join(",", roles)
                : "";

        /* 将权限列表转换为逗号分隔的字符串 */
        String permissionsStr = permissions != null && !permissions.isEmpty()
                ? String.join(",", permissions)
                : "";

        return JWT.create()
                .withSubject(user.getPhone())                     /* 使用手机号作为主题 */
                .withClaim("userId", user.getId())                /* 添加用户ID */
                .withClaim("phone", user.getPhone())              /* 添加手机号 */
                .withClaim("username", user.getUsername() != null ? user.getUsername() : "")  /* 添加用户名（可选） */
                .withClaim("email", user.getEmail() != null ? user.getEmail() : "")  /* 添加邮箱 */
                .withClaim("roles", rolesStr)                     /* 添加角色字符串（逗号分隔） */
                .withClaim("permissions", permissionsStr)         /* 添加权限字符串（逗号分隔） */
                .withIssuedAt(now)                                /* 设置签发时间 */
                .withExpiresAt(expiryDate)                        /* 设置过期时间 */
                .sign(algorithm);                                 /* 使用 HMAC256 算法签名 */
    }

    /**
     * 从 Token 中获取手机号
     *
     * @param token JWT Token
     * @return 手机号，如果 Token 无效则抛出异常
     */
    public String getPhoneFromToken(String token) {
        return decodeToken(token).getSubject();
    }

    /**
     * 从 Token 中获取用户名（兼容旧代码）
     *
     * @param token JWT Token
     * @return 用户名，如果不存在则返回空字符串
     */
    public String getUsernameFromToken(String token) {
        DecodedJWT jwt = decodeToken(token);
        return jwt.getClaim("username").asString();
    }

    /**
     * 从 Token 中获取角色列表
     *
     * @param token JWT Token
     * @return 角色列表，如果不存在角色则返回空列表
     */
    public List<String> getRolesFromToken(String token) {
        DecodedJWT jwt = decodeToken(token);
        String rolesStr = jwt.getClaim("roles").asString();
        
        /* 如果角色字符串为空，返回空列表 */
        if (rolesStr == null || rolesStr.isEmpty()) {
            return Collections.emptyList();
        }
        
        /* 将逗号分隔的字符串拆分并转换为列表 */
        return Arrays.asList(rolesStr.split(","));
    }

    /**
     * 从 Token 中获取权限列表
     *
     * @param token JWT Token
     * @return 权限列表，如果不存在权限则返回空列表
     */
    public List<String> getPermissionsFromToken(String token) {
        DecodedJWT jwt = decodeToken(token);
        String permissionsStr = jwt.getClaim("permissions").asString();
        
        /* 如果权限字符串为空，返回空列表 */
        if (permissionsStr == null || permissionsStr.isEmpty()) {
            return Collections.emptyList();
        }
        
        /* 将逗号分隔的字符串拆分并转换为列表 */
        return Arrays.asList(permissionsStr.split(","));
    }

    /**
     * 从 Token 中还原权限列表（包含角色和权限）
     * 
     * <p>角色会自动添加 ROLE_ 前缀以符合 Spring Security 规范。
     *
     * @param token JWT Token
     * @return SimpleGrantedAuthority 权限列表（角色 + 权限）
     */
    public List<org.springframework.security.core.authority.SimpleGrantedAuthority> getAuthoritiesFromToken(String token) {
        List<org.springframework.security.core.authority.SimpleGrantedAuthority> authorities = new ArrayList<>();

        /* 加载角色（添加 ROLE_ 前缀） */
        List<String> roles = getRolesFromToken(token);
        for (String role : roles) {
            if (role != null && !role.isEmpty()) {
                authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role));
            }
        }

        /* 加载权限 */
        List<String> permissions = getPermissionsFromToken(token);
        for (String permission : permissions) {
            if (permission != null && !permission.isEmpty()) {
                authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority(permission));
            }
        }

        return authorities;
    }

    /**
     * 验证 Token 是否有效（签名 & 过期）
     *
     * @param token JWT Token
     * @return true 如果 Token 有效，false 否则
     */
    public boolean validateToken(String token) {
        try {
            JWT.require(algorithm).build().verify(token);
            return true;
        } catch (JWTVerificationException e) {
            /* 记录验证失败的具体原因，便于调试和安全审计 */
            logger.warn("JWT Token 验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取 Token 过期时间（用于 Redis 黑名单策略）
     *
     * @param token JWT Token
     * @return 过期时间，如果 Token 无效则抛出异常
     */
    public Date getExpiresAt(String token) {
        return decodeToken(token).getExpiresAt();
    }

    /**
     * 解码并验证 Token（私有方法）
     *
     * @param token JWT Token
     * @return 解码后的 JWT 对象
     * @throws JWTVerificationException 如果 Token 无效或已过期
     */
    private DecodedJWT decodeToken(String token) {
        return JWT.require(algorithm).build().verify(token);
    }
}