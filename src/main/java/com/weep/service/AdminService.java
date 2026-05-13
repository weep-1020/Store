package com.weep.service;

import com.weep.dto.*;
import com.weep.response.PageResult;

/**
 * 管理员服务接口
 *
 * <p>提供管理员后台的核心管理功能，包括用户管理、角色权限分配、数据统计等。
 * 所有方法仅供拥有管理员权限的用户调用。
 */
public interface AdminService {

    /**
     * 分页查询用户列表
     *
     * @param keyword 搜索关键词（支持用户名、手机号模糊匹配）
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 分页结果，包含用户详情列表
     */
    PageResult<AdminUserDetailResponse> listUsers(String keyword, int page, int size);

    /**
     * 获取指定用户的详细信息
     *
     * @param id 用户ID
     * @return 用户详情响应对象，包含基本信息和角色权限
     */
    AdminUserDetailResponse getUserDetail(Long id);

    /**
     * 创建新用户
     *
     * @param request 创建用户请求，包含用户名、密码、手机号、邮箱等信息
     * @return 创建成功返回true，失败返回false
     */
    boolean createUser(AdminCreateUserRequest request);

    /**
     * 更新用户信息
     *
     * @param id 用户ID
     * @param request 更新用户请求，包含要修改的字段
     * @return 更新成功返回true，失败返回false
     */
    boolean updateUser(Long id, AdminUpdateUserRequest request);

    /**
     * 更新用户状态（启用/禁用）
     *
     * @param id 用户ID
     * @param status 用户状态（1-启用，0-禁用）
     * @return 更新成功返回true，失败返回false
     */
    boolean updateUserStatus(Long id, Integer status);

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 删除成功返回true，失败返回false
     */
    boolean deleteUser(Long id);

    /**
     * 修改用户密码
     *
     * @param id 用户ID
     * @param newPassword 新密码（明文）
     * @return 修改成功返回true，失败返回false
     */
    boolean changePassword(Long id, String newPassword);

    /**
     * 为用户分配角色
     *
     * @param userId 用户ID
     * @param request 角色分配请求，包含角色ID列表
     * @return 分配成功返回true，失败返回false
     */
    boolean assignRoles(Long userId, AssignRolesRequest request);

    /**
     * 获取仪表盘统计数据
     *
     * @return 仪表盘响应对象，包含订单、商品、用户等统计信息
     */
    DashboardResponse getDashboard();
}