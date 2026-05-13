package com.weep.service.impl;

import com.weep.dto.*;
import com.weep.entity.User;
import com.weep.mapper.PermissionMapper;
import com.weep.mapper.UserMapper;
import com.weep.response.PageResult;
import com.weep.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员服务实现类
 *
 * <p>实现管理员后台的核心管理功能，包括用户管理、角色权限分配、数据统计等。
 * 使用 @Transactional 注解保证事务一致性。
 */
@Service
public class AdminServiceImpl implements AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminServiceImpl.class);
    private static final String TOKEN_REDIS_PREFIX = "token:";

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PermissionMapper permissionMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 分页查询用户列表
     *
     * @param keyword 搜索关键词（支持用户名、手机号模糊匹配）
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 分页结果，包含用户详情列表
     */
    @Override
    public PageResult<AdminUserDetailResponse> listUsers(String keyword, int page, int size) {
        long total = userMapper.count(keyword);
        int offset = (page - 1) * size;
        List<User> users = userMapper.findByPage(keyword, offset, size);

        List<AdminUserDetailResponse> records = users.stream().map(u -> {
            AdminUserDetailResponse dto = new AdminUserDetailResponse();
            dto.setId(u.getId());
            dto.setUsername(u.getUsername());
            dto.setEmail(u.getEmail());
            dto.setPhone(u.getPhone());
            dto.setPic(u.getPic());
            dto.setStatus(u.getStatus());
            dto.setCreatedAt(u.getCreatedAt());
            dto.setUpdatedAt(u.getUpdatedAt());
            dto.setLastLoginAt(u.getLastLoginAt());
            dto.setPermissions(permissionMapper.findPermissionCodesByUserId(u.getId()));
            dto.setRoleId(u.getRoleId());
            return dto;
        }).collect(Collectors.toList());

        return new PageResult<>(records, total, page, size);
    }

    /**
     * 获取指定用户的详细信息
     *
     * @param id 用户ID
     * @return 用户详情响应对象，包含基本信息和角色权限
     */
    @Override
    public AdminUserDetailResponse getUserDetail(Long id) {
        User user = userMapper.findById(id);
        if (user == null) return null;

        AdminUserDetailResponse dto = new AdminUserDetailResponse();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setPic(user.getPic());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setLastLoginAt(user.getLastLoginAt());
        dto.setPermissions(permissionMapper.findPermissionCodesByUserId(id));
        dto.setRoleId(user.getRoleId());
        return dto;
    }

    /**
     * 创建新用户
     *
     * @param request 创建用户请求，包含用户名、密码、手机号、邮箱等信息
     * @return 创建成功返回true，失败返回false
     */
    @Override
    @Transactional
    public boolean createUser(AdminCreateUserRequest request) {
        try {
            if (userMapper.findByUsername(request.getUsername()) != null) {
                logger.warn("创建用户失败：用户名已存在 - {}", request.getUsername());
                return false;
            }
            if (request.getPhone() != null && !request.getPhone().isBlank()
                    && userMapper.findByPhone(request.getPhone()) != null) {
                logger.warn("创建用户失败：手机号已被使用 - {}", request.getPhone());
                return false;
            }
            if (request.getEmail() != null && !request.getEmail().isBlank()
                    && userMapper.findByEmail(request.getEmail()) != null) {
                logger.warn("创建用户失败：邮箱已被使用 - {}", request.getEmail());
                return false;
            }

            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setPhone(request.getPhone());
            user.setEmail(request.getEmail());
            user.setStatus(request.getStatus() != null ? request.getStatus() : 1);
            user.setRoleId(request.getRoleId() != null ? request.getRoleId() : 3L);

            LocalDateTime now = LocalDateTime.now();
            user.setCreatedAt(now);
            user.setUpdatedAt(now);

            int result = userMapper.insert(user);

            if (result > 0) {
                logger.info("管理员创建用户成功 - 用户名: {}", request.getUsername());
                return true;
            }
            return false;
        } catch (DuplicateKeyException e) {
            logger.warn("创建用户失败：违反唯一约束 - {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("创建用户异常 - 错误: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 更新用户信息
     *
     * @param id 用户ID
     * @param request 更新用户请求，包含要修改的字段
     * @return 更新成功返回true，失败返回false
     */
    @Override
    @Transactional
    public boolean updateUser(Long id, AdminUpdateUserRequest request) {
        try {
            User user = userMapper.findById(id);
            if (user == null) return false;

            if (request.getUsername() != null && !request.getUsername().isBlank()) {
                user.setUsername(request.getUsername());
            }
            if (request.getEmail() != null) {
                user.setEmail(request.getEmail());
            }
            if (request.getPhone() != null) {
                user.setPhone(request.getPhone());
            }
            if (request.getPic() != null) {
                user.setPic(request.getPic());
            }
            if (request.getStatus() != null) {
                user.setStatus(request.getStatus());
            }
            if (request.getRoleId() != null) {
                user.setRoleId(request.getRoleId());
            }

            int result = userMapper.update(user);

            if (result > 0) {
                logger.info("管理员更新用户成功 - ID: {}", id);
                return true;
            }
            return false;
        } catch (DuplicateKeyException e) {
            logger.warn("更新用户失败：违反唯一约束 - {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("更新用户异常 - ID: {}, 错误: {}", id, e.getMessage());
            return false;
        }
    }

    /**
     * 更新用户状态（启用/禁用）
     *
     * @param id 用户ID
     * @param status 用户状态（1-启用，0-禁用）
     * @return 更新成功返回true，失败返回false
     */
    @Override
    public boolean updateUserStatus(Long id, Integer status) {
        int result = userMapper.updateStatus(id, status);
        if (result > 0) {
            logger.info("管理员更新用户状态 - ID: {}, 状态: {}", id, status);
            return true;
        }
        return false;
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 删除成功返回true，失败返回false
     */
    @Override
    @Transactional
    public boolean deleteUser(Long id) {
        int result = userMapper.deleteById(id);
        if (result > 0) {
            logger.info("管理员删除用户成功 - ID: {}", id);
            return true;
        }
        return false;
    }

    /**
     * 修改用户密码
     *
     * @param id 用户ID
     * @param newPassword 新密码（明文）
     * @return 修改成功返回true，失败返回false
     */
    @Override
    public boolean changePassword(Long id, String newPassword) {
        User existingUser = userMapper.findById(id);
        if (existingUser == null) return false;

        User updateUser = new User();
        updateUser.setId(id);
        updateUser.setPassword(passwordEncoder.encode(newPassword));
        int result = userMapper.update(updateUser);

        if (result > 0) {
            String redisKey = TOKEN_REDIS_PREFIX + existingUser.getPhone();
            try {
                redisTemplate.delete(redisKey);
            } catch (Exception ex) {
                logger.warn("删除 Redis Token 失败 - 用户: {}", existingUser.getUsername());
            }
            logger.info("管理员重置密码成功 - ID: {}", id);
            return true;
        }
        return false;
    }

    /**
     * 为用户分配角色
     *
     * @param userId 用户ID
     * @param request 角色分配请求，包含角色ID列表
     * @return 分配成功返回true，失败返回false
     */
    @Override
    @Transactional
    public boolean assignRoles(Long userId, AssignRolesRequest request) {
        User user = new User();
        user.setId(userId);
        user.setRoleId(request.getRoleId());
        userMapper.update(user);
        logger.info("管理员分配角色成功 - 用户ID: {}, 角色ID: {}", userId, request.getRoleId());
        return true;
    }

    /**
     * 获取仪表盘统计数据
     *
     * @return 仪表盘响应对象，包含订单、商品、用户等统计信息
     */
    @Override
    public DashboardResponse getDashboard() {
        DashboardResponse dashboard = new DashboardResponse();

        long totalUsers = userMapper.count("");
        dashboard.setTotalUsers(totalUsers);

        try {
            dashboard.setActiveUsers(userMapper.countByStatus(1));
            dashboard.setDisabledUsers(userMapper.countByStatus(0));
        } catch (Exception e) {
            dashboard.setActiveUsers(0);
            dashboard.setDisabledUsers(0);
        }

        try {
            dashboard.setTotalOrders(userMapper.countOrders(null));
            dashboard.setPendingOrders(userMapper.countOrders(0));
            dashboard.setPaidOrders(userMapper.countOrders(1));
            dashboard.setCompletedOrders(userMapper.countOrders(3));
        } catch (Exception e) {
            dashboard.setTotalOrders(0);
            dashboard.setPendingOrders(0);
        }
        return dashboard;
    }
}