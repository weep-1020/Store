package com.weep.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户信息控制器
 * <p>
 * 提供用户相关的 REST API 接口，这些接口需要认证后才能访问
 * </p>
 *
 * @author Store Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    /**
     * 获取当前用户信息
     * <p>
     * 该接口需要携带有效的 Token 才能访问
     * </p>
     *
     * @return 用户信息
     */
    @GetMapping("/info")
    public Map<String, Object> getUserInfo() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "获取用户信息成功");
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", 1);
        userInfo.put("username", "admin");
        userInfo.put("email", "admin@example.com");
        userInfo.put("phone", "13800138000");
        
        result.put("data", userInfo);
        return result;
    }

    /**
     * 获取用户列表
     * <p>
     * 该接口需要携带有效的 Token 才能访问
     * </p>
     *
     * @return 用户列表
     */
    @GetMapping("/list")
    public Map<String, Object> getUserList() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "获取用户列表成功");
        result.put("data", new String[]{"admin", "user", "test"});
        return result;
    }
}
