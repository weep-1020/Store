package com.weep.service;

import com.weep.dto.LoginRequest;
import com.weep.dto.LoginResponse;
import com.weep.entity.User;
import com.weep.mapper.PermissionMapper;
import com.weep.mapper.RoleMapper;
import com.weep.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 认证服务类
 * <p>
 * 提供用户认证相关的业务逻辑，包括：
 * - 用户登录验证
 * - JWT Token 生成和验证
 * - 用户信息管理
 * </p>
 *
 * @author Store Team
 * @version 3.0
 */
@Service
public class AuthService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    /**
     * JWT 工具类
     */
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户服务类
     */
    @Autowired
    private UserService userService;

    /**
     * 角色 Mapper
     */
    @Autowired
    private RoleMapper roleMapper;

    /**
     * 权限 Mapper
     */
    @Autowired
    private PermissionMapper permissionMapper;

    /**
     * 用户登录
     * <p>
     * 验证用户名和密码，如果验证成功则生成并返回 Token
     * </p>
     *
     * @param loginRequest 登录请求对象，包含用户名和密码
     * @return 登录响应对象，包含 Token 和用户信息
     */
    public LoginResponse login(LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        // 参数验证
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            logger.warn("登录失败：用户名或密码为空");
            return LoginResponse.fail("用户名和密码不能为空");
        }

        // 通过 UserService 验证用户登录
        User user = userService.login(username, password);
        
        if (user == null) {
            return LoginResponse.fail("用户名或密码错误");
        }

        // 查询用户的角色和权限
        java.util.List<String> roles = roleMapper.findRoleCodesByUsername(username);
        java.util.List<String> permissions = permissionMapper.findPermissionCodesByUsername(username);
        
        logger.info("用户 {} 的角色: {}, 权限: {}", username, roles, permissions);

        // 生成包含完整用户信息、角色和权限的 JWT Token
        String token = jwtUtil.generateTokenWithUserDetails(user, roles, permissions);

        logger.info("用户登录成功 - {}", username);
        return LoginResponse.success(token, username);
    }

    /**
     * 验证 JWT Token 是否有效
     *
     * @param token 待验证的 Token
     * @return 如果 Token 有效返回 true，否则返回 false
     */
    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        // 去除 Bearer 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        return jwtUtil.validateToken(token);
    }

    /**
     * 根据 JWT Token 获取用户名
     *
     * @param token 认证 Token
     * @return 用户名，如果 Token 无效则返回 null
     */
    public String getUsernameByToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }

        // 去除 Bearer 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            return jwtUtil.getUsernameFromToken(token);
        } catch (Exception e) {
            logger.warn("从 Token 获取用户名失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 用户登出
     * <p>
     * JWT 是无状态的，客户端删除 Token 即可
     * </p>
     *
     * @param token 要失效的 Token
     * @return 始终返回 true（JWT 无状态特性）
     */
    public boolean logout(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        // 去除 Bearer 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 验证 Token 是否有效
        if (jwtUtil.validateToken(token)) {
            String username = jwtUtil.getUsernameFromToken(token);
            logger.info("用户登出成功 - {}", username);
            return true;
        }

        return false;
    }

    /**
     * 根据用户名加载用户详情
     * <p>
     * 该方法由 Spring Security 调用，用于在认证过程中加载用户信息。
     * 会查询用户的角色和权限，并构建包含授权信息的 UserDetails 对象。
     * </p>
     *
     * @param username 用户名
     * @return UserDetails 对象，包含用户信息和授权信息
     * @throws UsernameNotFoundException 当用户不存在时抛出
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("加载用户详情 - 用户名: {}", username);
        
        // 从数据库查询用户信息
        User user = userService.findByUsername(username);
        
        if (user == null) {
            logger.warn("用户不存在 - 用户名: {}", username);
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        
        // 检查用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            logger.warn("用户已被禁用 - 用户名: {}", username);
            throw new UsernameNotFoundException("用户已被禁用: " + username);
        }
        
        // 查询用户的角色和权限
        List<String> roles = roleMapper.findRoleCodesByUsername(username);
        List<String> permissions = permissionMapper.findPermissionCodesByUsername(username);
        
        logger.debug("用户 {} 的角色: {}, 权限: {}", username, roles, permissions);
        
        // 构建授权列表（角色和权限都作为授权信息）
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        
        // 添加角色（以 ROLE_ 前缀标识）
        if (roles != null) {
            roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
        }
        
        // 添加权限
        if (permissions != null) {
            permissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));
        }
        
        // 返回 Spring Security 的 UserDetails 对象
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}
