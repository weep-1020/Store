package com.weep.service.impl;

import com.weep.entity.Role;
import com.weep.mapper.PermissionMapper;
import com.weep.mapper.RoleMapper;
import com.weep.mapper.UserMapper;
import com.weep.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色服务实现类
 *
 * <p>实现角色的管理功能，包括角色的增删改查和权限分配操作。
 * 用于 RBAC（基于角色的访问控制）体系中的角色管理。
 */
@Service
public class RoleServiceImpl implements RoleService {

    private static final Logger logger = LoggerFactory.getLogger(RoleServiceImpl.class);

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 查询所有角色列表
     *
     * @return 角色列表
     */
    @Override
    public List<Role> findAll() {
        return roleMapper.findAll();
    }

    /**
     * 根据ID查询角色
     *
     * @param id 角色ID
     * @return 角色对象，不存在则返回null
     */
    @Override
    public Role findById(Long id) {
        return roleMapper.findById(id);
    }

    /**
     * 创建角色
     *
     * <p>角色代码必须唯一，不能重复创建相同角色代码的角色。
     *
     * @param role 角色对象
     * @return 创建成功返回角色对象，失败返回null
     */
    @Override
    public Role create(Role role) {
        try {
            if (roleMapper.findByRoleCode(role.getRoleCode()) != null) {
                logger.warn("创建角色失败：角色代码已存在 - {}", role.getRoleCode());
                return null;
            }
            roleMapper.insert(role);
            logger.info("角色创建成功 - {}", role.getRoleCode());
            return role;
        } catch (DuplicateKeyException e) {
            logger.warn("创建角色失败：违反唯一约束 - {}", e.getMessage());
            return null;
        }
    }

    /**
     * 更新角色
     *
     * @param role 角色对象（必须包含id）
     * @return 更新成功返回更新后的角色对象，失败返回null
     */
    @Override
    public Role update(Role role) {
        try {
            Role existing = roleMapper.findById(role.getId());
            if (existing == null) return null;
            roleMapper.update(role);
            logger.info("角色更新成功 - ID: {}", role.getId());
            return roleMapper.findById(role.getId());
        } catch (DuplicateKeyException e) {
            logger.warn("更新角色失败：违反唯一约束 - {}", e.getMessage());
            return null;
        }
    }

    /**
     * 删除角色
     *
     * <p>如果仍有用户使用该角色，则无法删除。
     *
     * @param id 角色ID
     * @return 删除成功返回true，失败返回false
     */
    @Override
    @Transactional
    public boolean delete(Long id) {
        Role existing = roleMapper.findById(id);
        if (existing == null) return false;
        if (userMapper.countByRoleId(id) > 0) {
            logger.warn("角色删除失败 - ID: {}, 仍有用户使用该角色", id);
            return false;
        }
        roleMapper.deleteById(id);
        logger.info("角色删除成功 - ID: {}", id);
        return true;
    }

    /**
     * 为角色分配权限
     *
     * <p>先清除该角色原有的所有权限，然后批量添加新权限。
     *
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     */
    @Override
    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        permissionMapper.deleteRolePermissions(roleId);
        if (permissionIds != null) {
            for (Long permId : permissionIds) {
                permissionMapper.insertRolePermission(roleId, permId);
            }
        }
        logger.info("角色权限分配成功 - 角色ID: {}", roleId);
    }
}