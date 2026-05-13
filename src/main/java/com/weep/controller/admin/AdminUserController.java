package com.weep.controller.admin;

import com.weep.dto.*;
import com.weep.response.PageResult;
import com.weep.response.Result;
import com.weep.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员用户管理控制器
 *
 * <p>负责处理管理员对用户的管理操作，包括用户列表查询、创建、更新、删除、角色分配等。
 * 所有接口需要管理员权限。
 *
 * <p>主要功能：
 * <ul>
 *   <li>查询用户列表（带分页和关键字搜索）</li>
 *   <li>获取用户详情</li>
 *   <li>创建用户</li>
 *   <li>更新用户信息</li>
 *   <li>更新用户状态（启用/禁用）</li>
 *   <li>重置用户密码</li>
 *   <li>为用户分配角色</li>
 *   <li>删除用户</li>
 * </ul>
 *
 * @author Weep
 * @version 1.0
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(AdminUserController.class);

    /**
     * 管理员服务接口
     */
    private final AdminService adminService;

    /**
     * 构造函数，依赖注入服务
     *
     * @param adminService 管理员服务
     */
    public AdminUserController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * 获取用户列表（分页）
     *
     * <p>支持按关键字搜索（用户名、手机号、邮箱），分页返回用户列表。
     *
     * @param keyword 搜索关键字（可选）
     * @param page    页码（默认1）
     * @param size    每页数量（默认10）
     * @return 用户列表
     *         <ul>
     *           <li>200: 查询成功</li>
     *         </ul>
     */
    @GetMapping
    public Result<PageResult<AdminUserDetailResponse>> list(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<AdminUserDetailResponse> result = adminService.listUsers(keyword, page, size);
        return Result.success(result);
    }

    /**
     * 获取用户详情
     *
     * <p>根据ID获取用户详细信息，包含用户基本信息和角色信息。
     *
     * @param id 用户ID
     * @return 用户详情
     *         <ul>
     *           <li>200: 查询成功</li>
     *           <li>404: 用户不存在</li>
     *         </ul>
     */
    @GetMapping("/{id}")
    public Result<AdminUserDetailResponse> detail(@PathVariable Long id) {
        AdminUserDetailResponse detail = adminService.getUserDetail(id);
        if (detail == null) {
            return Result.error(404, "用户不存在");
        }
        return Result.success(detail);
    }

    /**
     * 创建用户（管理员）
     *
     * <p>管理员创建新用户，需要提供用户名和密码。
     *
     * @param request 创建用户请求体，包含：
     *                <ul>
     *                  <li>username: 用户名（必填）</li>
     *                  <li>password: 密码（必填，≥6位）</li>
     *                  <li>phone: 手机号（可选）</li>
     *                  <li>email: 邮箱（可选）</li>
     *                  <li>roleIds: 角色ID列表（可选）</li>
     *                </ul>
     * @return 创建结果
     *         <ul>
     *           <li>200: 创建成功</li>
     *           <li>400: 参数错误（用户名/密码为空、密码过短）</li>
     *           <li>500: 创建失败（用户名/手机号/邮箱已存在）</li>
     *         </ul>
     */
    @PostMapping
    public Result<Void> create(@RequestBody AdminCreateUserRequest request) {
        // 参数校验：用户名和密码不能为空
        if (request.getUsername() == null || request.getUsername().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()) {
            return Result.error(400, "用户名和密码不能为空");
        }

        // 参数校验：密码长度
        if (request.getPassword().length() < 6) {
            return Result.error(400, "密码长度不能少于6位");
        }

        // 创建用户
        boolean success = adminService.createUser(request);
        if (success) {
            logger.info("管理员创建用户成功 - 用户名: {}", request.getUsername());
            return Result.success("创建成功", null);
        }
        return Result.error(500, "创建失败，用户名/手机号/邮箱可能已存在");
    }

    /**
     * 更新用户信息（管理员）
     *
     * <p>管理员更新用户的基本信息。
     *
     * @param id      用户ID
     * @param request 更新用户请求体，包含：
     *                <ul>
     *                  <li>username: 用户名（可选）</li>
     *                  <li>phone: 手机号（可选）</li>
     *                  <li>email: 邮箱（可选）</li>
     *                </ul>
     * @return 更新结果
     *         <ul>
     *           <li>200: 更新成功</li>
     *           <li>404: 用户不存在或更新失败</li>
     *         </ul>
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody AdminUpdateUserRequest request) {
        boolean success = adminService.updateUser(id, request);
        if (success) {
            logger.info("管理员更新用户成功 - ID: {}", id);
            return Result.success("更新成功", null);
        }
        return Result.error(404, "用户不存在或更新失败");
    }

    /**
     * 更新用户状态（启用/禁用）
     *
     * <p>管理员启用或禁用用户账号。状态值必须为0（禁用）或1（启用）。
     *
     * @param id   用户ID
     * @param body 请求体，包含status字段
     * @return 更新结果
     *         <ul>
     *           <li>200: 状态更新成功</li>
     *           <li>400: 状态值必须为0或1</li>
     *           <li>500: 状态更新失败</li>
     *         </ul>
     */
    @PutMapping("/{id}/status")
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestBody AdminUpdateUserRequest body) {
        Integer status = body.getStatus();

        // 参数校验：状态值必须为0或1
        if (status == null || (status != 0 && status != 1)) {
            return Result.error(400, "状态值必须为 0 或 1");
        }

        // 更新用户状态
        boolean success = adminService.updateUserStatus(id, status);
        if (success) {
            logger.info("管理员更新用户状态 - ID: {}, 状态: {}", id, status);
            return Result.success("状态更新成功", null);
        }
        return Result.error(500, "状态更新失败");
    }

    /**
     * 重置用户密码（管理员）
     *
     * <p>管理员重置用户密码，新密码长度不能少于6位。
     *
     * @param id      用户ID
     * @param request 重置密码请求体，包含newPassword字段
     * @return 重置结果
     *         <ul>
     *           <li>200: 密码重置成功</li>
     *           <li>400: 新密码长度不能少于6位</li>
     *           <li>404: 用户不存在或重置失败</li>
     *         </ul>
     */
    @PutMapping("/{id}/password")
    public Result<Void> changePassword(@PathVariable Long id, @RequestBody AdminChangePasswordRequest request) {
        // 参数校验：新密码长度
        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            return Result.error(400, "新密码长度不能少于6位");
        }

        // 重置密码
        boolean success = adminService.changePassword(id, request.getNewPassword());
        if (success) {
            logger.info("管理员重置用户密码 - ID: {}", id);
            return Result.success("密码重置成功", null);
        }
        return Result.error(404, "用户不存在或重置失败");
    }

    /**
     * 为用户分配角色
     *
     * <p>批量为用户分配角色，会先清空原有角色再重新分配。
     *
     * @param id      用户ID
     * @param request 角色分配请求体，包含roleIds列表
     * @return 分配结果
     *         <ul>
     *           <li>200: 角色分配成功</li>
     *           <li>500: 角色分配失败</li>
     *         </ul>
     */
    @PutMapping("/{id}/roles")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody AssignRolesRequest request) {
        boolean success = adminService.assignRoles(id, request);
        if (success) {
            return Result.success("角色分配成功", null);
        }
        return Result.error(500, "角色分配失败");
    }

    /**
     * 删除用户（管理员）
     *
     * <p>管理员删除指定用户账号。
     *
     * @param id 用户ID
     * @return 删除结果
     *         <ul>
     *           <li>200: 删除成功</li>
     *           <li>500: 删除失败</li>
     *         </ul>
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = adminService.deleteUser(id);
        if (success) {
            logger.info("管理员删除用户成功 - ID: {}", id);
            return Result.success("删除成功", null);
        }
        return Result.error(500, "删除失败");
    }
}