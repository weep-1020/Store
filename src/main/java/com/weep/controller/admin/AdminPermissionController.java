package com.weep.controller.admin;

import com.weep.entity.Permission;
import com.weep.response.Result;
import com.weep.service.PermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限管理控制器
 *
 * <p>负责处理权限的增删改查操作，所有接口需要管理员权限。
 *
 * <p>主要功能：
 * <ul>
 *   <li>查询所有权限</li>
 *   <li>获取权限详情</li>
 *   <li>创建权限</li>
 *   <li>更新权限</li>
 *   <li>删除权限</li>
 * </ul>
 *
 * @author Weep
 * @version 1.0
 */
@RestController
@RequestMapping("/api/admin/permissions")
public class AdminPermissionController {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(AdminPermissionController.class);

    /**
     * 权限服务接口
     */
    private final PermissionService permissionService;

    /**
     * 构造函数，依赖注入服务
     *
     * @param permissionService 权限服务
     */
    public AdminPermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * 获取所有权限列表
     *
     * <p>返回所有权限记录。
     *
     * @return 权限列表
     *         <ul>
     *           <li>200: 查询成功</li>
     *         </ul>
     */
    @GetMapping
    public Result<List<Permission>> list() {
        return Result.success(permissionService.findAll());
    }

    /**
     * 获取权限详情
     *
     * <p>根据ID获取权限详细信息。
     *
     * @param id 权限ID
     * @return 权限详情
     *         <ul>
     *           <li>200: 查询成功</li>
     *           <li>404: 权限不存在</li>
     *         </ul>
     */
    @GetMapping("/{id}")
    public Result<Permission> detail(@PathVariable Long id) {
        Permission perm = permissionService.findById(id);
        if (perm == null) {
            return Result.error(404, "权限不存在");
        }
        return Result.success(perm);
    }

    /**
     * 创建权限
     *
     * <p>创建新的权限记录。权限名称和权限代码不能为空且唯一。
     *
     * @param permission 权限对象，包含：
     *                  <ul>
     *                    <li>permissionName: 权限名称（必填）</li>
     *                    <li>permissionCode: 权限代码（必填，唯一）</li>
     *                    <li>description: 权限描述（可选）</li>
     *                  </ul>
     * @return 创建结果
     *         <ul>
     *           <li>200: 创建成功</li>
     *           <li>400: 参数错误（名称或代码为空、代码已存在）</li>
     *           <li>500: 创建失败</li>
     *         </ul>
     */
    @PostMapping
    public Result<Permission> create(@RequestBody Permission permission) {
        // 参数校验：权限名称和代码不能为空
        if (permission.getPermissionName() == null || permission.getPermissionName().isBlank()
                || permission.getPermissionCode() == null || permission.getPermissionCode().isBlank()) {
            return Result.error(400, "权限名称和代码不能为空");
        }

        // 创建权限
        Permission created = permissionService.create(permission);
        if (created != null) {
            logger.info("创建权限成功 - {}", permission.getPermissionCode());
            return Result.success("创建成功", created);
        }
        return Result.error(400, "权限代码已存在");
    }

    /**
     * 更新权限
     *
     * <p>更新指定权限的信息。
     *
     * @param id         权限ID
     * @param permission 更新后的权限对象
     * @return 更新结果
     *         <ul>
     *           <li>200: 更新成功</li>
     *           <li>404: 权限不存在或更新失败</li>
     *         </ul>
     */
    @PutMapping("/{id}")
    public Result<Permission> update(@PathVariable Long id, @RequestBody Permission permission) {
        permission.setId(id);
        Permission updated = permissionService.update(permission);
        if (updated != null) {
            return Result.success("更新成功", updated);
        }
        return Result.error(404, "权限不存在或更新失败");
    }

    /**
     * 删除权限
     *
     * <p>删除指定的权限记录。
     *
     * @param id 权限ID
     * @return 删除结果
     *         <ul>
     *           <li>200: 删除成功</li>
     *           <li>404: 权限不存在</li>
     *           <li>500: 删除失败</li>
     *         </ul>
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = permissionService.delete(id);
        if (success) {
            logger.info("删除权限成功 - ID: {}", id);
            return Result.success("删除成功", null);
        }
        return Result.error(404, "权限不存在");
    }
}