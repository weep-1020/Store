package com.weep.controller;

import com.weep.dto.LoginRequest;
import com.weep.dto.LoginResponse;
import com.weep.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * <p>
 * 提供用户认证相关的 REST API 接口，包括：
 * - 用户登录
 * - 用户登出
 * - Token 验证
 * </p>
 *
 * @author Store Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    /**
     * 认证服务
     */
    @Autowired
    private AuthService authService;

    /**
     * 用户登录接口
     * <p>
     * 接收用户名和密码，验证通过后返回认证 Token
     * </p>
     *
     * @param loginRequest 登录请求对象，包含用户名和密码
     * @return 登录响应对象，包含 Token 和用户信息
     */
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
        logger.info("收到登录请求 - 用户名: {}", loginRequest.getUsername());
        
        try {
            LoginResponse response = authService.login(loginRequest);
            
            if (response.getCode() == 200) {
                logger.info("登录成功 - 用户名: {}", loginRequest.getUsername());
            } else {
                logger.warn("登录失败 - 用户名: {}, 原因: {}", 
                        loginRequest.getUsername(), response.getMessage());
            }
            
            return response;
        } catch (Exception e) {
            logger.error("登录处理异常 - 用户名: {}, 错误: {}", 
                    loginRequest.getUsername(), e.getMessage());
            return LoginResponse.fail("服务器内部错误");
        }
    }

    /**
     * 用户登出接口
     * <p>
     * 使当前用户的 Token 失效
     * </p>
     *
     * @param authorization 请求头中的 Authorization Token
     * @return 登出结果
     */
    @PostMapping("/logout")
    public LoginResponse logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        logger.info("收到登出请求");
        
        if (authorization == null || authorization.isEmpty()) {
            logger.warn("登出失败：未提供 Token");
            return LoginResponse.fail("未提供认证令牌");
        }

        try {
            boolean success = authService.logout(authorization);
            
            if (success) {
                logger.info("登出成功");
                return new LoginResponse(200, "登出成功", null, null);
            } else {
                logger.warn("登出失败：Token 无效");
                return LoginResponse.fail("无效的 Token");
            }
        } catch (Exception e) {
            logger.error("登出处理异常 - 错误: {}", e.getMessage());
            return LoginResponse.fail("服务器内部错误");
        }
    }

    /**
     * 验证 Token 接口
     * <p>
     * 检查提供的 Token 是否有效
     * </p>
     *
     * @param authorization 请求头中的 Authorization Token
     * @return 验证结果
     */
    @GetMapping("/validate")
    public LoginResponse validateToken(@RequestHeader(value = "Authorization", required = false) String authorization) {
        logger.info("收到 Token 验证请求");
        
        if (authorization == null || authorization.isEmpty()) {
            logger.warn("Token 验证失败：未提供 Token");
            return LoginResponse.fail("未提供认证令牌");
        }

        try {
            boolean isValid = authService.validateToken(authorization);
            
            if (isValid) {
                String username = authService.getUsernameByToken(authorization);
                logger.info("Token 验证成功 - 用户: {}", username);
                return new LoginResponse(200, "Token 有效", authorization, username);
            } else {
                logger.warn("Token 验证失败：Token 无效");
                return LoginResponse.fail("Token 无效或已过期");
            }
        } catch (Exception e) {
            logger.error("Token 验证异常 - 错误: {}", e.getMessage());
            return LoginResponse.fail("服务器内部错误");
        }
    }
}
