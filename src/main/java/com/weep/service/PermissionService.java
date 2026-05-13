package com.weep.service;

import com.weep.entity.Permission;

import java.util.List;

/**
 * 权限服务接口
 *
 * <p>提供权限管理功能，包括权限的增删改查操作。
 * 权限是RBAC（基于角色的访问控制）体系中的最小权限单元。
 */
public interface PermissionService {

    /**
     * 查询所有权限
     *
     * @return 权限列表
     */
    List<Permission> findAll();

    /**
     * 根据ID查询权限
     *
     * @param id 权限ID
     * @return 权限对象，不存在则返回null
     */
    Permission findById(Long id);

    /**
     * 创建权限
     *
     * @param permission 权限对象
     * @return 创建成功的权限对象
     */
    Permission create(Permission permission);

    /**
     * 更新权限
     *
     * @param permission 权限对象（必须包含id）
     * @return 更新成功的权限对象
     */
    Permission update(Permission permission);

    /**
     * 删除权限
     *
     * @param id 权限ID
     * @return 删除成功返回true，失败返回false
     */
    boolean delete(Long id);
}