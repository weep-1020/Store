package com.weep.service;

import com.weep.entity.Role;

import java.util.List;

/**
 * 角色服务接口
 *
 * <p>提供角色管理功能，包括角色的增删改查以及权限分配操作。
 * 角色是RBAC（基于角色的访问控制）体系中的核心单元。
 */
public interface RoleService {

    /**
     * 查询所有角色
     *
     * @return 角色列表
     */
    List<Role> findAll();

    /**
     * 根据ID查询角色
     *
     * @param id 角色ID
     * @return 角色对象，不存在则返回null
     */
    Role findById(Long id);

    /**
     * 创建角色
     *
     * @param role 角色对象
     * @return 创建成功的角色对象
     */
    Role create(Role role);

    /**
     * 更新角色
     *
     * @param role 角色对象（必须包含id）
     * @return 更新成功的角色对象
     */
    Role update(Role role);

    /**
     * 删除角色
     *
     * @param id 角色ID
     * @return 删除成功返回true，失败返回false
     */
    boolean delete(Long id);

    /**
     * 为角色分配权限
     *
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     */
    void assignPermissions(Long roleId, List<Long> permissionIds);
}