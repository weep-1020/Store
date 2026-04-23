package com.weep.controller;

import com.weep.entity.User;
import com.weep.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户信息控制器
 * <p>
 * 提供用户相关的 REST API 接口，这些接口需要认证后才能访问
 * </p>
 *
 * @author Store Team
 * @version 2.0
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    /**
     * 用户服务类
     */
    @Autowired
    private UserService userService;

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
        
        try {
//              从 Token 中获取当前用户ID，这里暂时返回模拟数据
            User user = userService.findById(1L);
            
            if (user != null) {
                result.put("code", 200);
                result.put("message", "获取用户信息成功");
                
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("email", user.getEmail());
                userInfo.put("phone", user.getPhone());
                userInfo.put("status", user.getStatus());
                userInfo.put("createdAt", user.getCreatedAt());
                
                result.put("data", userInfo);
            } else {
                result.put("code", 404);
                result.put("message", "用户不存在");
            }
        } catch (Exception e) {
            logger.error("获取用户信息异常 - 错误: {}", e.getMessage());
            result.put("code", 500);
            result.put("message", "服务器内部错误");
        }
        
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
        
        try {
            List<User> users = userService.findAll();
            
            if (users != null) {
                result.put("code", 200);
                result.put("message", "获取用户列表成功");
                result.put("data", users);
                result.put("total", users.size());
            } else {
                result.put("code", 500);
                result.put("message", "获取用户列表失败");
            }
        } catch (Exception e) {
            logger.error("获取用户列表异常 - 错误: {}", e.getMessage());
            result.put("code", 500);
            result.put("message", "服务器内部错误");
        }
        
        return result;
    }

    /**
     * 根据ID获取用户信息
     * <p>
     * 该接口需要携带有效的 Token 才能访问
     * </p>
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public Map<String, Object> getUserById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            User user = userService.findById(id);
            
            if (user != null) {
                result.put("code", 200);
                result.put("message", "获取用户信息成功");
                result.put("data", user);
            } else {
                result.put("code", 404);
                result.put("message", "用户不存在");
            }
        } catch (Exception e) {
            logger.error("获取用户信息异常 - ID: {}, 错误: {}", id, e.getMessage());
            result.put("code", 500);
            result.put("message", "服务器内部错误");
        }
        
        return result;
    }
}
