package com.weep.controller;

import com.weep.dto.LoginResponse;
import com.weep.entity.User;
import com.weep.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户注册控制器
 * <p>
 * 提供用户注册相关的 REST API 接口
 * </p>
 *
 * @author Store Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/auth")
public class RegisterController {

    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);

    /**
     * 用户服务类
     */
    @Autowired
    private UserService userService;

    /**
     * 用户注册接口
     * <p>
     * 接收用户注册信息，验证通过后创建新用户
     * </p>
     *
     * @param user 用户对象，包含 username, password, email, phone
     * @return 注册结果
     */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        
        logger.info("收到注册请求 - 用户名: {}", user.getUsername());
        
        try {
            // 参数验证
            if (user.getUsername() == null || user.getUsername().isEmpty()) {
                logger.warn("注册失败：用户名为空");
                result.put("code", 400);
                result.put("message", "用户名不能为空");
                return result;
            }
            
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                logger.warn("注册失败：密码为空");
                result.put("code", 400);
                result.put("message", "密码不能为空");
                return result;
            }
            
            // 密码长度验证
            if (user.getPassword().length() < 6) {
                logger.warn("注册失败：密码长度不足");
                result.put("code", 400);
                result.put("message", "密码长度不能少于6位");
                return result;
            }

            // 调用服务层进行注册
            boolean success = userService.register(user);
            
            if (success) {
                logger.info("用户注册成功 - 用户名: {}", user.getUsername());
                result.put("code", 200);
                result.put("message", "注册成功");
                
                // 返回用户信息（不包含密码）
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("email", user.getEmail());
                userInfo.put("phone", user.getPhone());
                result.put("data", userInfo);
            } else {
                logger.warn("用户注册失败 - 用户名: {}", user.getUsername());
                result.put("code", 400);
                result.put("message", "注册失败，用户名可能已存在");
            }
        } catch (Exception e) {
            logger.error("用户注册异常 - 用户名: {}, 错误: {}", user.getUsername(), e.getMessage());
            result.put("code", 500);
            result.put("message", "服务器内部错误");
        }
        
        return result;
    }
}
