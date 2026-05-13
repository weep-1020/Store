package com.weep.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.weep.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT 工具类（基于 Auth0 java-jwt）
 * <p>
 * 提供 JWT Token 的生成、验证和解析功能
 * </p>
 *
 * @author Store Team
 * @version 2.0
 */
@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    /**
     * JWT 密钥
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Token 过期时间（毫秒）
     */
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 获取签名算法
     *
     * @return Algorithm 对象
     */
    private Algorithm getAlgorithm() {
        return Algorithm.HMAC256(secret);
    }

    /**
     * 生成 JWT Token
     *
     * @param username 用户名
     * @return JWT Token 字符串
     */
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    /**
     * 生成 JWT Token（带角色和权限）
     *
     * @param user        用户对象
     * @param roles       角色列表
     * @param permissions 权限列表
     * @return JWT Token 字符串
     */
    public String generateTokenWithUserDetails(User user, List<String> roles, List<String> permissions) {
        // 构建包含用户详细信息、角色和权限的自定义声明
        Map<String, Object> claims = new HashMap<>();
        
        // 添加用户基本信息到声明中
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("email", user.getEmail() != null ? user.getEmail() : "");
        claims.put("phone", user.getPhone() != null ? user.getPhone() : "");
        claims.put("status", user.getStatus());
        claims.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : "");
        
        // 将角色列表转换为逗号分隔的字符串并添加到声明中
        if (roles != null && !roles.isEmpty()) {
            claims.put("roles", String.join(",", roles));
        }
        
        // 将权限列表转换为逗号分隔的字符串并添加到声明中
        if (permissions != null && !permissions.isEmpty()) {
            claims.put("permissions", String.join(",", permissions));
        }
        
        return createToken(claims, user.getUsername());
    }

    /**
     * 生成 JWT Token（带自定义声明）
     *
     * @param claims   自定义声明
     * @param username 用户名
     * @return JWT Token 字符串
     */
    public String generateTokenWithClaims(Map<String, Object> claims, String username) {
        return createToken(claims, username);
    }

    /**
     * 创建 Token
     *
     * @param claims   声明信息
     * @param username 用户名
     * @return JWT Token 字符串
     */
    private String createToken(Map<String, Object> claims, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        com.auth0.jwt.JWTCreator.Builder builder = JWT.create()
                .withSubject(username)
                .withIssuedAt(now)
                .withExpiresAt(expiryDate);

        // 添加自定义声明
        for (Map.Entry<String, Object> entry : claims.entrySet()) {
            builder.withClaim(entry.getKey(), entry.getValue().toString());
        }

        String token = builder.sign(getAlgorithm());

        logger.debug("为用户生成 JWT Token - 用户: {}", username);
        return token;
    }

    /**
     * 从 Token 中获取用户名
     *
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        DecodedJWT jwt = decodeToken(token);
        return jwt.getSubject();
    }

    /**
     * 从 Token 中获取角色列表
     *
     * @param token JWT Token
     * @return 角色代码列表
     */
    public List<String> getRolesFromToken(String token) {
        try {
            DecodedJWT jwt = decodeToken(token);
            String rolesStr = jwt.getClaim("roles").asString();
            if (rolesStr != null && !rolesStr.isEmpty()) {
                return Arrays.asList(rolesStr.split(","));
            }
        } catch (Exception e) {
            logger.warn("从 Token 中获取角色失败: {}", e.getMessage());
        }
        return new ArrayList<>();
    }

    /**
     * 从 Token 中获取权限列表
     *
     * @param token JWT Token
     * @return 权限代码列表
     */
    public List<String> getPermissionsFromToken(String token) {
        try {
            DecodedJWT jwt = decodeToken(token);
            String permissionsStr = jwt.getClaim("permissions").asString();
            if (permissionsStr != null && !permissionsStr.isEmpty()) {
                return Arrays.asList(permissionsStr.split(","));
            }
        } catch (Exception e) {
            logger.warn("从 Token 中获取权限失败: {}", e.getMessage());
        }
        return new ArrayList<>();
    }

    /**
     * 从 Token 中获取用户ID
     *
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            DecodedJWT jwt = decodeToken(token);
            return jwt.getClaim("userId").asLong();
        } catch (Exception e) {
            logger.warn("从 Token 中获取用户ID失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 Token 中获取邮箱
     *
     * @param token JWT Token
     * @return 邮箱
     */
    public String getEmailFromToken(String token) {
        try {
            DecodedJWT jwt = decodeToken(token);
            return jwt.getClaim("email").asString();
        } catch (Exception e) {
            logger.warn("从 Token 中获取邮箱失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 Token 中获取手机号
     *
     * @param token JWT Token
     * @return 手机号
     */
    public String getPhoneFromToken(String token) {
        try {
            DecodedJWT jwt = decodeToken(token);
            return jwt.getClaim("phone").asString();
        } catch (Exception e) {
            logger.warn("从 Token 中获取手机号失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 Token 中获取用户状态
     *
     * @param token JWT Token
     * @return 用户状态
     */
    public Integer getStatusFromToken(String token) {
        try {
            DecodedJWT jwt = decodeToken(token);
            return jwt.getClaim("status").asInt();
        } catch (Exception e) {
            logger.warn("从 Token 中获取用户状态失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 Token 中获取创建时间
     *
     * @param token JWT Token
     * @return 创建时间字符串
     */
    public String getCreatedAtFromToken(String token) {
        try {
            DecodedJWT jwt = decodeToken(token);
            return jwt.getClaim("createdAt").asString();
        } catch (Exception e) {
            logger.warn("从 Token 中获取创建时间失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 解码 Token
     *
     * @param token JWT Token
     * @return DecodedJWT 对象
     */
    private DecodedJWT decodeToken(String token) {
        JWTVerifier verifier = JWT.require(getAlgorithm()).build();
        return verifier.verify(token);
    }

    /**
     * 验证 Token 是否有效
     *
     * @param token JWT Token
     * @return 如果 Token 有效返回 true，否则返回 false
     */
    public boolean validateToken(String token) {
        try {
            decodeToken(token);
            return true;
        } catch (JWTVerificationException e) {
            logger.warn("JWT Token 验证失败: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.warn("JWT Token 验证异常: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查 Token 是否过期
     *
     * @param token JWT Token
     * @return 如果 Token 已过期返回 true，否则返回 false
     */
    public boolean isTokenExpired(String token) {
        try {
            DecodedJWT jwt = decodeToken(token);
            return jwt.getExpiresAt().before(new Date());
        } catch (Exception e) {
            logger.warn("检查 Token 过期状态失败: {}", e.getMessage());
            return true;
        }
    }

    /**
     * 从 Token 中获取过期时间
     *
     * @param token JWT Token
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        DecodedJWT jwt = decodeToken(token);
        return jwt.getExpiresAt();
    }

    /**
     * 刷新 Token
     * <p>
     * 如果 Token 未过期，则生成一个新的 Token
     * </p>
     *
     * @param token 旧的 JWT Token
     * @return 新的 JWT Token，如果旧 Token 无效则返回 null
     */
    public String refreshToken(String token) {
        if (!validateToken(token)) {
            return null;
        }

        String username = getUsernameFromToken(token);
        return generateToken(username);
    }
}
