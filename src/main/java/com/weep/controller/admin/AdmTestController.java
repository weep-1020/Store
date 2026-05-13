package com.weep.controller.admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员测试控制器
 *
 * <p>用于测试管理员权限认证的控制器，提供两个测试接口验证不同角色的权限访问。
 *
 * <p>主要功能：
 * <ul>
 *   <li>test1: 仅超级管理员可访问</li>
 *   <li>test2: 超级管理员和商家均可访问</li>
 * </ul>
 *
 * @author Weep
 * @version 1.0
 */
@RestController
@RequestMapping("/api/admin")
public class AdmTestController {

    /**
     * 权限认证测试接口1（仅超级管理员）
     *
     * <p>测试超级管理员角色权限，只有拥有 SUPER_ADMIN 角色的用户才能访问。
     */
    @GetMapping("/test1")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public void chaxun(){
        System.out.println("权限认证1");
    }

    /**
     * 权限认证测试接口2（超级管理员和商家）
     *
     * <p>测试超级管理员和商家角色权限，拥有 SUPER_ADMIN 或 MERCHANT 角色的用户均可访问。
     */
    @GetMapping("/test2")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT')")
    public void cs(){
        System.out.println("权限认证2");
    }

}