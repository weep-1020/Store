package com.weep.service;

import com.weep.dto.LoginRequest;
import com.weep.dto.LoginResponse;
import com.weep.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

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
 * @version 2.0
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    /**
     * JWT 工具类
     */
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 模拟用户数据库（实际项目中应该从数据库查询）
     */
    private static final Map<String, String> USER_DATABASE = new HashMap<>();

    static {
        // 初始化测试用户（密码应该是加密存储的）
        USER_DATABASE.put("admin", "123456");
        USER_DATABASE.put("user", "password");
        USER_DATABASE.put("test", "test123");
    }

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

        // 验证用户是否存在
        String storedPassword = USER_DATABASE.get(username);
        if (storedPassword == null) {
            logger.warn("登录失败：用户不存在 - {}", username);
            return LoginResponse.fail("用户不存在");
        }

        // 验证密码（实际项目中应该使用 BCrypt 等加密算法比对）
        if (!storedPassword.equals(password)) {
            logger.warn("登录失败：密码错误 - {}", username);
            return LoginResponse.fail("密码错误");
        }

        // 生成 JWT Token
        String token = jwtUtil.generateToken(username);

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
}
