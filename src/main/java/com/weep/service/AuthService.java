package com.weep.service;

import java.util.Map;

/**
 * 认证服务接口
 *
 * <p>提供用户认证相关的核心功能，包括登录、登出、Token验证等。
 * 基于JWT实现无状态认证，并通过Redis维护Token白名单。
 */
public interface AuthService {

    /**
     * 用户登录（使用手机号 + 密码）
     *
     * @param phone 手机号
     * @param password 密码（明文）
     * @return JWT Token，如果登录失败返回 null
     */
    String login(String phone, String password);

    /**
     * 用户登录并返回完整用户信息（通过手机号或用户名）
     *
     * @param identifier 手机号或用户名
     * @param password 密码（明文）
     * @return 包含 Token、用户名、权限列表和 userInfo 的 Map
     */
    Map<String, Object> loginWithUserInfo(String identifier, String password);

    /**
     * 用户登出
     *
     * <p>从Redis中移除该用户的Token，使其失效。
     *
     * @param phone 手机号
     * @return 登出成功返回 true
     */
    boolean logout(String phone);

    /**
     * 验证 Token 是否在 Redis 中存在
     *
     * <p>用于验证Token是否有效（未过期且未被注销）。
     *
     * @param phone 手机号
     * @param token JWT Token
     * @return Token 存在返回 true
     */
    boolean validateTokenInRedis(String phone, String token);
}