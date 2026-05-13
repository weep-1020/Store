package com.weep.controller.admin;

import com.weep.dto.AssignPermissionsRequest;
import com.weep.entity.Role;
import com.weep.mapper.PermissionMapper;
import com.weep.response.Result;
import com.weep.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 角色管理控制器
 *
 * <p>负责处理角色的增删改查及权限分配操作，所有接口需要管理员权限。
 *
 * <p>主要功能：
 * <ul>
 *   <li>查询所有角色（含权限列表）</li>
 *   <li>获取角色详情</li>
 *   <li>创建角色</li>
 *   <li>更新角色</li>
 *   <li>为角色分配权限</li>
 *   <li>删除角色</li>
 * </ul>
 *
 * @author Weep
 * @version 1.0
 */
@RestController
@RequestMapping("/api/admin/roles")
public class AdminRoleController {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(AdminRoleController.class);

    /**
     * 角色服务接口
     */
    private final RoleService roleService;

    /**
     * 权限Mapper接口
     */
    private final PermissionMapper permissionMapper;

    /**
     * 构造函数，依赖注入服务
     *
     * @param roleService      角色服务
     * @param permissionMapper 权限Mapper
     */
    public AdminRoleController(RoleService roleService, PermissionMapper permissionMapper) {
        this.roleService = roleService;
        this.permissionMapper = permissionMapper;
    }

    /**
     * 获取所有角色列表（含权限信息）
     *
     * <p>返回所有角色及其关联的权限ID列表。
     *
     * @return 角色列表，每个角色包含权限ID列表
     *         <ul>
     *           <li>200: 查询成功</li>
     *         </ul>
     */
    @GetMapping
    public Result<List<Map<String, Object>>> list() {
        // 查询所有角色
        List<Role> roles = roleService.findAll();

        // 组装角色信息和权限ID列表
        List<Map<String, Object>> result = roles.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("roleName", r.getRoleName());
            map.put("roleCode", r.getRoleCode());
            map.put("description", r.getDescription());
            // 查询角色关联的权限ID列表
            List<Long> permIds = permissionMapper.findPermissionIdsByRoleId(r.getId());
            map.put("permissionIds", permIds);
            return map;
        }).collect(Collectors.toList());

        return Result.success(result);
    }

    /**
     * 获取角色详情
     *
     * <p>根据ID获取角色详细信息及其关联的权限ID列表。
     *
     * @param id 角色ID
     * @return 角色详情
     *         <ul>
     *           <li>200: 查询成功</li>
     *           <li>404: 角色不存在</li>
     *         </ul>
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        // 查询角色
        Role role = roleService.findById(id);
        if (role == null) {
            return Result.error(404, "角色不存在");
        }

        // 组装角色信息
        Map<String, Object> map = new HashMap<>();
        map.put("id", role.getId());
        map.put("roleName", role.getRoleName());
        map.put("roleCode", role.getRoleCode());
        map.put("description", role.getDescription());
        // 查询角色关联的权限ID列表
        map.put("permissionIds", permissionMapper.findPermissionIdsByRoleId(id));

        return Result.success(map);
    }

    /**
     * 创建角色
     *
     * <p>创建新的角色记录。角色名称和角色代码不能为空且唯一。
     *
     * @param role 角色对象，包含：
     *            <ul>
     *              <li>roleName: 角色名称（必填）</li>
     *              <li>roleCode: 角色代码（必填，唯一）</li>
     *              <li>description: 角色描述（可选）</li>
     *            </ul>
     * @return 创建结果
     *         <ul>
     *           <li>200: 创建成功</li>
     *           <li>400: 参数错误（名称或代码为空、代码已存在）</li>
     *           <li>500: 创建失败</li>
     *         </ul>
     */
    @PostMapping
    public Result<Role> create(@RequestBody Role role) {
        // 参数校验：角色名称和代码不能为空
        if (role.getRoleName() == null || role.getRoleName().isBlank()
                || role.getRoleCode() == null || role.getRoleCode().isBlank()) {
            return Result.error(400, "角色名称和代码不能为空");
        }

        // 创建角色
        Role created = roleService.create(role);
        if (created != null) {
            logger.info("创建角色成功 - {}", role.getRoleCode());
            return Result.success("创建成功", created);
        }
        return Result.error(400, "角色代码已存在");
    }

    /**
     * 更新角色
     *
     * <p>更新指定角色的信息。
     *
     * @param id   角色ID
     * @param role 更新后的角色对象
     * @return 更新结果
     *         <ul>
     *           <li>200: 更新成功</li>
     *           <li>404: 角色不存在或更新失败</li>
     *         </ul>
     */
    @PutMapping("/{id}")
    public Result<Role> update(@PathVariable Long id, @RequestBody Role role) {
        role.setId(id);
        Role updated = roleService.update(role);
        if (updated != null) {
            return Result.success("更新成功", updated);
        }
        return Result.error(404, "角色不存在或更新失败");
    }

    /**
     * 为角色分配权限
     *
     * <p>批量为角色分配权限，会先清空原有权限再重新分配。
     *
     * @param id      角色ID
     * @param request 权限分配请求体，包含permissionIds列表
     * @return 分配结果
     *         <ul>
     *           <li>200: 权限分配成功</li>
     *           <li>404: 角色不存在</li>
     *           <li>500: 分配失败</li>
     *         </ul>
     */
    @PutMapping("/{id}/permissions")
    public Result<Void> assignPermissions(@PathVariable Long id, @RequestBody AssignPermissionsRequest request) {
        roleService.assignPermissions(id, request.getPermissionIds());
        return Result.success("权限分配成功", null);
    }

    /**
     * 删除角色
     *
     * <p>删除指定的角色记录。
     *
     * @param id 角色ID
     * @return 删除结果
     *         <ul>
     *           <li>200: 删除成功</li>
     *           <li>404: 角色不存在</li>
     *           <li>500: 删除失败（可能存在关联用户）</li>
     *         </ul>
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = roleService.delete(id);
        if (success) {
            logger.info("删除角色成功 - ID: {}", id);
            return Result.success("删除成功", null);
        }
        return Result.error(404, "角色不存在");
    }
}