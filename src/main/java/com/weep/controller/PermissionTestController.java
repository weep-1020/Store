package com.weep.controller;

import com.weep.annotation.RequirePermission;
import com.weep.annotation.RequireRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 权限测试控制器
 * <p>
 * 用于测试 RBAC 权限控制系统
 * </p>
 *
 * @author Store Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api")
public class PermissionTestController {

    private static final Logger logger = LoggerFactory.getLogger(PermissionTestController.class);

    /**
     * 公开接口 - 无需认证即可访问
     */
    @GetMapping("/test/public")
    public Map<String, Object> publicEndpoint() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "这是一个公开接口，无需认证即可访问");
        return result;
    }

    /**
     * 管理员接口 - 仅 SUPER_ADMIN 角色可访问
     */
    @GetMapping("/test/admin")
    @RequireRole({"SUPER_ADMIN"})
    public Map<String, Object> adminEndpoint() {
        logger.info("访问管理员接口");
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "这是管理员接口，仅 SUPER_ADMIN 角色可访问");
        result.put("data", "管理员专属数据");
        return result;
    }

    /**
     * 商家接口 - SUPER_ADMIN 或 MERCHANT 角色可访问
     */
    @GetMapping("/test/merchant")
    @RequireRole(value = {"SUPER_ADMIN", "MERCHANT"}, logical = RequireRole.Logical.OR)
    public Map<String, Object> merchantEndpoint() {
        logger.info("访问商家接口");
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "这是商家接口，SUPER_ADMIN 或 MERCHANT 角色可访问");
        result.put("data", "商家专属数据");
        return result;
    }

    /**
     * 商品管理接口 - 需要 product:manage 权限
     */
    @GetMapping("/test/product/manage")
    @RequirePermission({"product:manage"})
    public Map<String, Object> productManageEndpoint() {
        logger.info("访问商品管理接口");
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "这是商品管理接口，需要 product:manage 权限");
        result.put("data", "商品管理功能");
        return result;
    }

    /**
     * 订单查看接口 - 需要 order:view 权限
     */
    @GetMapping("/test/order/view")
    @RequirePermission({"order:view"})
    public Map<String, Object> orderViewEndpoint() {
        logger.info("访问订单查看接口");
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "这是订单查看接口，需要 order:view 权限");
        result.put("data", "订单列表数据");
        return result;
    }

    /**
     * 用户管理接口 - 需要 user:manage 权限或 SUPER_ADMIN 角色
     */
    @GetMapping("/test/user/manage")
    @RequireRole({"SUPER_ADMIN"})
    @RequirePermission(value = {"user:manage"}, logical = RequirePermission.Logical.OR)
    public Map<String, Object> userManageEndpoint() {
        logger.info("访问用户管理接口");
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "这是用户管理接口，需要 SUPER_ADMIN 角色或 user:manage 权限");
        result.put("data", "用户管理功能");
        return result;
    }
}
