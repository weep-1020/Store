package com.weep.service.impl;

import com.weep.entity.User;
import com.weep.mapper.UserMapper;
import com.weep.response.PageResult;
import com.weep.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户服务实现类
 *
 * <p>实现用户管理的核心功能，包括用户注册、查询、更新、删除、密码修改等操作。
 * 使用 BCrypt 进行密码加密，确保用户密码安全。
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private static final String TOKEN_REDIS_PREFIX = "token:";

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户对象，不存在则返回null
     */
    @Override
    public User findByUsername(String username) {
        try {
            return userMapper.findByUsername(username);
        } catch (Exception e) {
            logger.error("查询用户失败 - 用户名: {}, 错误: {}", username, e.getMessage());
            return null;
        }
    }

    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 用户对象，不存在则返回null
     */
    @Override
    public User findById(Long id) {
        try {
            return userMapper.findById(id);
        } catch (Exception e) {
            logger.error("查询用户失败 - ID: {}, 错误: {}", id, e.getMessage());
            return null;
        }
    }

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱地址
     * @return 用户对象，不存在则返回null
     */
    @Override
    public User findByEmail(String email) {
        try {
            return userMapper.findByEmail(email);
        } catch (Exception e) {
            logger.error("查询用户失败 - 邮箱: {}, 错误: {}", email, e.getMessage());
            return null;
        }
    }

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号码
     * @return 用户对象，不存在则返回null
     */
    @Override
    public User findByPhone(String phone) {
        try {
            return userMapper.findByPhone(phone);
        } catch (Exception e) {
            logger.error("查询用户失败 - 手机号: {}, 错误: {}", phone, e.getMessage());
            return null;
        }
    }

    /**
     * 查询所有用户列表（密码字段置空）
     *
     * @return 用户列表，无数据则返回空列表
     */
    @Override
    public List<User> findAll() {
        try {
            List<User> users = userMapper.findAll();
            for (User u : users) {
                u.setPassword(null);
            }
            return users;
        } catch (Exception e) {
            logger.error("查询所有用户失败 - 错误: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 分页查询用户列表
     *
     * @param keyword 搜索关键词（用户名/邮箱/手机号）
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 分页结果
     */
    @Override
    public PageResult<User> findByPage(String keyword, int page, int size) {
        try {
            long total = userMapper.count(keyword);
            int offset = (page - 1) * size;
            List<User> list = userMapper.findByPage(keyword, offset, size);
            for (User u : list) {
                u.setPassword(null);
            }
            return new PageResult<>(list, total, page, size);
        } catch (Exception e) {
            logger.error("分页查询用户失败 - 错误: {}", e.getMessage());
            return new PageResult<>(new ArrayList<>(), 0, page, size);
        }
    }

    /**
     * 用户注册
     *
     * <p>校验规则：
     * <ul>
     *   <li>用户名必须唯一</li>
     *   <li>邮箱必须唯一（如果提供）</li>
     *   <li>手机号必须唯一（如果提供）</li>
     * </ul>
     * 
     * <p>默认设置：
     * <ul>
     *   <li>状态为启用(1)</li>
     *   <li>角色为普通用户(3)</li>
     *   <li>密码使用BCrypt加密</li>
     * </ul>
     *
     * @param user 用户对象（包含username、password等必填字段）
     * @return 注册成功返回true，失败返回false
     */
    @Override
    public boolean register(User user) {
        try {
            if (userMapper.findByUsername(user.getUsername()) != null) {
                logger.warn("注册失败：用户名已存在 - {}", user.getUsername());
                return false;
            }
            if (user.getEmail() != null && userMapper.findByEmail(user.getEmail()) != null) {
                logger.warn("注册失败：邮箱已被使用 - {}", user.getEmail());
                return false;
            }
            if (user.getPhone() != null && userMapper.findByPhone(user.getPhone()) != null) {
                logger.warn("注册失败：手机号已被使用 - {}", user.getPhone());
                return false;
            }

            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);

            if (user.getStatus() == null) {
                user.setStatus(1);
            }

            LocalDateTime now = LocalDateTime.now();
            if (user.getCreatedAt() == null) {
                user.setCreatedAt(now);
            }
            user.setUpdatedAt(now);

            user.setRoleId(3L);

            int result = userMapper.insert(user);

            if (result > 0) {
                logger.info("用户注册成功 - 用户名: {}", user.getUsername());
                return true;
            } else {
                logger.warn("用户注册失败 - 用户名: {}", user.getUsername());
                return false;
            }
        } catch (DuplicateKeyException e) {
            logger.warn("注册失败：违反唯一约束 - 用户名: {}, 错误: {}", user.getUsername(), e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("用户注册异常 - 用户名: {}, 错误: {}", user.getUsername(), e.getMessage());
            return false;
        }
    }

    /**
     * 更新用户信息
     *
     * @param user 用户对象（必须包含id）
     * @return 更新成功返回true，失败返回false
     */
    @Override
    public boolean update(User user) {
        try {
            if (user.getId() == null) {
                logger.warn("更新用户失败：用户ID为空");
                return false;
            }

            User existingUser = userMapper.findById(user.getId());
            if (existingUser == null) {
                logger.warn("更新用户失败：用户不存在 - ID: {}", user.getId());
                return false;
            }

            int result = userMapper.update(user);

            if (result > 0) {
                logger.info("用户信息更新成功 - ID: {}", user.getId());
                return true;
            } else {
                logger.warn("用户信息更新失败 - ID: {}", user.getId());
                return false;
            }
        } catch (DuplicateKeyException e) {
            logger.warn("更新用户失败：违反唯一约束 - ID: {}, 错误: {}", user.getId(), e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("用户信息更新异常 - ID: {}, 错误: {}", user.getId(), e.getMessage());
            return false;
        }
    }

    /**
     * 根据ID删除用户
     *
     * @param id 用户ID
     * @return 删除成功返回true，失败返回false
     */
    @Override
    public boolean deleteById(Long id) {
        try {
            if (id == null) {
                logger.warn("删除用户失败：用户ID为空");
                return false;
            }

            User existingUser = userMapper.findById(id);
            if (existingUser == null) {
                logger.warn("删除用户失败：用户不存在 - ID: {}", id);
                return false;
            }

            int result = userMapper.deleteById(id);

            if (result > 0) {
                logger.info("用户删除成功 - ID: {}", id);
                return true;
            } else {
                logger.warn("用户删除失败 - ID: {}", id);
                return false;
            }
        } catch (Exception e) {
            logger.error("用户删除异常 - ID: {}, 错误: {}", id, e.getMessage());
            return false;
        }
    }

    /**
     * 更新用户状态
     *
     * @param id 用户ID
     * @param status 用户状态（0: 禁用, 1: 启用）
     * @return 更新成功返回true，失败返回false
     */
    @Override
    public boolean updateStatus(Long id, Integer status) {
        try {
            if (id == null || status == null) {
                logger.warn("更新用户状态失败：参数为空 - ID: {}, Status: {}", id, status);
                return false;
            }

            User existingUser = userMapper.findById(id);
            if (existingUser == null) {
                logger.warn("更新用户状态失败：用户不存在 - ID: {}", id);
                return false;
            }

            int result = userMapper.updateStatus(id, status);

            if (result > 0) {
                logger.info("用户状态更新成功 - ID: {}, 新状态: {}", id, status);
                return true;
            } else {
                logger.warn("用户状态更新失败 - ID: {}", id);
                return false;
            }
        } catch (Exception e) {
            logger.error("用户状态更新异常 - ID: {}, 错误: {}", id, e.getMessage());
            return false;
        }
    }

    /**
     * 修改用户密码
     *
     * <p>密码修改成功后，会自动删除用户在Redis中的Token，强制用户重新登录。
     *
     * @param id 用户ID
     * @param newPassword 新密码
     * @return 修改成功返回true，失败返回false
     */
    @Override
    public boolean changePassword(Long id, String newPassword) {
        try {
            if (id == null || newPassword == null || newPassword.isEmpty()) {
                logger.warn("修改密码失败：参数无效 - ID: {}", id);
                return false;
            }

            User existingUser = userMapper.findById(id);
            if (existingUser == null) {
                logger.warn("修改密码失败：用户不存在 - ID: {}", id);
                return false;
            }

            String encodedPassword = passwordEncoder.encode(newPassword);

            User updateUser = new User();
            updateUser.setId(id);
            updateUser.setPassword(encodedPassword);

            int result = userMapper.update(updateUser);

            if (result > 0) {
                String redisKey = TOKEN_REDIS_PREFIX + existingUser.getPhone();
                try {
                    redisTemplate.delete(redisKey);
                    logger.info("用户密码修改后 Token 已失效 - 手机号: {}", existingUser.getPhone());
                } catch (Exception ex) {
                    logger.warn("删除 Redis Token 失败 - 用户: {}, 错误: {}", existingUser.getUsername(), ex.getMessage());
                }
                logger.info("用户密码修改成功 - ID: {}", id);
                return true;
            } else {
                logger.warn("用户密码修改失败 - ID: {}", id);
                return false;
            }
        } catch (Exception e) {
            logger.error("用户密码修改异常 - ID: {}, 错误: {}", id, e.getMessage());
            return false;
        }
    }

    /**
     * 更新用户最后登录时间
     *
     * @param id 用户ID
     */
    @Override
    public void updateLastLoginAt(Long id) {
        try {
            userMapper.updateLastLoginAt(id);
        } catch (Exception e) {
            logger.error("更新登录时间失败 - ID: {}, 错误: {}", id, e.getMessage());
        }
    }

}