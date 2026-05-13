package com.weep.service;

import com.weep.entity.User;
import com.weep.response.PageResult;

import java.util.List;

/**
 * 用户服务接口
 *
 * <p>提供用户管理的核心功能，包括用户注册、查询、更新、删除、状态管理等操作。
 */
public interface UserService {

    /**
     * 修改用户密码
     *
     * @param id 用户ID
     * @param newPassword 新密码（明文）
     * @return 修改成功返回 true，失败返回 false
     */
    boolean changePassword(Long id, String newPassword);

    /**
     * 更新用户状态
     *
     * @param id 用户ID
     * @param status 用户状态（1-启用，0-禁用）
     * @return 更新成功返回 true，失败返回 false
     */
    boolean updateStatus(Long id, Integer status);

    /**
     * 注册新用户
     *
     * @param user 用户对象（需包含 username, password, email, phone 等信息）
     * @return 注册成功返回 true，失败返回 false
     */
    boolean register(User user);

    /**
     * 查询所有用户
     *
     * @return 用户列表，如果没有用户则返回空列表
     */
    List<User> findAll();

    /**
     * 分页查询用户
     *
     * @param keyword 搜索关键词
     * @param page 页码（从1开始）
     * @param size 每页数量
     * @return 分页结果
     */
    PageResult<User> findByPage(String keyword, int page, int size);

    /**
     * 更新用户信息
     *
     * @param user 用户对象（需包含 id）
     * @return 更新成功返回 true，失败返回 false
     */
    boolean update(User user);

    /**
     * 根据ID删除用户
     *
     * @param id 用户ID
     * @return 删除成功返回 true，失败返回 false
     */
    boolean deleteById(Long id);

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户对象，如果不存在则返回 null
     */
    User findByPhone(String phone);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱地址
     * @return 用户对象，如果不存在则返回 null
     */
    User findByEmail(String email);

    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 用户对象，如果不存在则返回 null
     */
    User findById(Long id);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户对象，如果不存在则返回 null
     */
    User findByUsername(String username);
    
    /**
     * 更新用户最后登录时间
     *
     * @param id 用户ID
     */
    void updateLastLoginAt(Long id);
    
    /**
     * 根据ID更新用户信息
     *
     * @param user 用户对象（需包含 id）
     */
    // void updateById(User user);
}