package com.weep.service.impl;

import com.weep.entity.Permission;
import com.weep.mapper.PermissionMapper;
import com.weep.service.PermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 权限服务实现类
 *
 * <p>实现权限的管理功能，包括权限的增删改查操作。
 * 用于 RBAC（基于角色的访问控制）体系中的权限管理。
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    private static final Logger logger = LoggerFactory.getLogger(PermissionServiceImpl.class);

    @Autowired
    private PermissionMapper permissionMapper;

    /**
     * 查询所有权限列表
     *
     * @return 权限列表
     */
    @Override
    public List<Permission> findAll() {
        return permissionMapper.findAll();
    }

    /**
     * 根据ID查询权限
     *
     * @param id 权限ID
     * @return 权限对象，不存在则返回null
     */
    @Override
    public Permission findById(Long id) {
        return permissionMapper.findById(id);
    }

    /**
     * 创建权限
     *
     * <p>权限代码必须唯一，不能重复创建相同权限代码的权限。
     *
     * @param permission 权限对象
     * @return 创建成功返回权限对象，失败返回null
     */
    @Override
    public Permission create(Permission permission) {
        try {
            if (permissionMapper.findByPermissionCode(permission.getPermissionCode()) != null) {
                logger.warn("创建权限失败：权限代码已存在 - {}", permission.getPermissionCode());
                return null;
            }
            permissionMapper.insert(permission);
            logger.info("权限创建成功 - {}", permission.getPermissionCode());
            return permission;
        } catch (DuplicateKeyException e) {
            logger.warn("创建权限失败：违反唯一约束 - {}", e.getMessage());
            return null;
        }
    }

    /**
     * 更新权限
     *
     * @param permission 权限对象（必须包含id）
     * @return 更新成功返回更新后的权限对象，失败返回null
     */
    @Override
    public Permission update(Permission permission) {
        try {
            Permission existing = permissionMapper.findById(permission.getId());
            if (existing == null) return null;
            permissionMapper.update(permission);
            logger.info("权限更新成功 - ID: {}", permission.getId());
            return permissionMapper.findById(permission.getId());
        } catch (DuplicateKeyException e) {
            logger.warn("更新权限失败：违反唯一约束 - {}", e.getMessage());
            return null;
        }
    }

    /**
     * 删除权限
     *
     * @param id 权限ID
     * @return 删除成功返回true，失败返回false
     */
    @Override
    public boolean delete(Long id) {
        Permission existing = permissionMapper.findById(id);
        if (existing == null) return false;
        permissionMapper.deleteById(id);
        logger.info("权限删除成功 - ID: {}", id);
        return true;
    }
}