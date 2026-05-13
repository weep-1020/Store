package com.weep.service.impl;

import com.weep.entity.User;
import com.weep.mapper.PermissionMapper;
import com.weep.mapper.RoleMapper;
import com.weep.service.AuthService;
import com.weep.service.UserService;
import com.weep.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 认证服务实现类
 *
 * <p>实现用户认证相关的核心功能，包括登录、登出、Token验证等。
 * 基于JWT实现无状态认证，并通过Redis维护Token白名单。
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final String TOKEN_REDIS_PREFIX = "token:";

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    /**
     * 用户登录（使用手机号 + 密码）
     *
     * @param phone 手机号
     * @param password 密码（明文）
     * @return JWT Token，如果登录失败返回 null
     */
    @Override
    public String login(String phone, String password) {
        try {
            User user = userService.findByPhone(phone);

            if (user == null) {
                logger.error("用户登录失败：手机号不存在 - {}", phone);
                return null;
            }

            UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(phone, password);

            Authentication authentication = authenticationManager.authenticate(authToken);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(auth -> auth.startsWith("ROLE_"))
                    .map(auth -> auth.substring(5))
                    .toList();

            List<String> permissions = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(auth -> !auth.startsWith("ROLE_"))
                    .toList();

            String token = jwtUtil.generateTokenWithUserDetails(user, roles, permissions);

            String redisKey = TOKEN_REDIS_PREFIX + user.getPhone();
            Long expirationMs = jwtUtil.getExpiration();
            try {
                redisTemplate.opsForValue().set(redisKey, token, expirationMs, TimeUnit.MILLISECONDS);
            } catch (Exception ex) {
                logger.warn("Redis 存储 Token 失败，但登录仍成功 - 用户: {}, 错误: {}",
                    user.getUsername(), ex.getMessage());
            }

            // 更新用户最后登录时间
            user.setLastLoginAt(java.time.LocalDateTime.now());
            userService.update(user);
            return token;

        } catch (Exception e) {
            logger.error("用户登录异常 - 手机号: {}, 错误: {}", phone, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 用户登录并返回完整用户信息（通过手机号或用户名）
     *
     * @param identifier 手机号或用户名
     * @param password 密码（明文）
     * @return 包含 Token、用户名、权限列表和 userInfo 的 Map
     */
    @Override
    public Map<String, Object> loginWithUserInfo(String identifier, String password) {
        try {
            User user;
            String phone;

            // identifier 可以是手机号或用户名
            if (identifier != null && identifier.matches("^1[3-9]\\d{9}$")) {
                user = userService.findByPhone(identifier);
                phone = identifier;
            } else {
                user = userService.findByUsername(identifier);
                phone = user != null ? user.getPhone() : null;
            }

            if (user == null) {
                logger.error("用户登录失败：用户不存在 - identifier: {}", identifier);
                return null;
            }

            UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(phone, password);

            Authentication authentication = authenticationManager.authenticate(authToken);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(auth -> auth.startsWith("ROLE_"))
                    .map(auth -> auth.substring(5))
                    .toList();

            List<String> permissions = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(auth -> !auth.startsWith("ROLE_"))
                    .toList();

            String token = jwtUtil.generateTokenWithUserDetails(user, roles, permissions);

            String redisKey = TOKEN_REDIS_PREFIX + user.getPhone();
            Long expirationMs = jwtUtil.getExpiration();
            try {
                redisTemplate.opsForValue().set(redisKey, token, expirationMs, TimeUnit.MILLISECONDS);
            } catch (Exception ex) {
                logger.warn("Redis 存储 Token 失败，但登录仍成功 - 用户: {}, 错误: {}",
                    user.getUsername(), ex.getMessage());
            }

            userService.updateLastLoginAt(user.getId());

            // 组装返回数据：同时支持嵌套结构(userInfo)和扁平字段(兼容旧测试)
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("username", user.getUsername());
            result.put("authorities", authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));

            // 用户信息（不含密码）
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("phone", user.getPhone());
            userInfo.put("pic", user.getPic());
            userInfo.put("status", user.getStatus());
            userInfo.put("createdAt", user.getCreatedAt());
            userInfo.put("roles", roles);
            userInfo.put("permissions", permissions);
            result.put("userInfo", userInfo);

            return result;

        } catch (Exception e) {
            logger.error("用户登录异常 - identifier: {}, 错误: {}", identifier, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 用户登出
     *
     * <p>从Redis中移除该用户的Token，使其失效。
     *
     * @param phone 手机号
     * @return 登出成功返回 true
     */
    @Override
    public boolean logout(String phone) {
        try {
            String redisKey = TOKEN_REDIS_PREFIX + phone;
            Boolean deleted = redisTemplate.delete(redisKey);

            if (Boolean.TRUE.equals(deleted)) {
                logger.info("用户登出成功，Token 已从 Redis 删除 - 手机号: {}", phone);
                return true;
            } else {
                logger.warn("用户登出失败：Redis 中未找到 Token - 手机号: {}", phone);
                return false;
            }
        } catch (Exception e) {
            logger.error("用户登出异常 - 手机号: {}, 错误: {}", phone, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 验证 Token 是否在 Redis 中存在
     *
     * <p>用于验证Token是否有效（未过期且未被注销）。
     *
     * @param phone 手机号
     * @param token JWT Token
     * @return Token 存在返回 true
     */
    @Override
    public boolean validateTokenInRedis(String phone, String token) {
        try {
            String redisKey = TOKEN_REDIS_PREFIX + phone;
            String redisToken = (String) redisTemplate.opsForValue().get(redisKey);

            return redisToken != null && redisToken.equals(token);
        } catch (Exception e) {
            logger.error("验证 Token 异常 - 手机号: {}, 错误: {}", phone, e.getMessage(), e);
            return false;
        }
    }
}