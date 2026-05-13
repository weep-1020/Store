package com.weep.service;

import com.weep.entity.User;
import com.weep.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户服务类
 * <p>
 * 提供用户相关的业务逻辑处理，包括：
 * - 用户注册
 * - 用户查询
 * - 用户信息更新
 * - 用户状态管理
 * </p>
 *
 * @author Store Team
 * @version 1.0
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    /**
     * 用户数据访问接口
     */
    @Autowired
    private UserMapper userMapper;

    /**
     * BCrypt 密码加密器
     */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户对象，如果不存在则返回 null
     */
    public User findByUsername(String username) {
        try {
            return userMapper.findByUsername(username);
        } catch (Exception e) {
            logger.error("查询用户失败 - 用户名: {}, 错误: {}", username, e.getMessage());
            return null;
        }
    }

    /**
     * 根据用户ID查询用户
     *
     * @param id 用户ID
     * @return 用户对象，如果不存在则返回 null
     */
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
     * @return 用户对象，如果不存在则返回 null
     */
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
     * @param phone 手机号
     * @return 用户对象，如果不存在则返回 null
     */
    public User findByPhone(String phone) {
        try {
            return userMapper.findByPhone(phone);
        } catch (Exception e) {
            logger.error("查询用户失败 - 手机号: {}, 错误: {}", phone, e.getMessage());
            return null;
        }
    }

    /**
     * 查询所有用户
     *
     * @return 用户列表
     */
    public List<User> findAll() {
        try {
            return userMapper.findAll();
        } catch (Exception e) {
            logger.error("查询所有用户失败 - 错误: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 注册新用户
     * <p>
     * 该方法会：
     * 1. 检查用户名是否已存在
     * 2. 对密码进行 BCrypt 加密
     * 3. 设置默认状态为启用
     * 4. 设置创建时间为当前时间
     * 5. 插入数据库
     * </p>
     *
     * @param user 用户对象（需包含 username, password, email, phone）
     * @return 注册成功返回 true，失败返回 false
     */
    public boolean register(User user) {
        try {
            // 检查用户名是否已存在
            if (userMapper.findByUsername(user.getUsername()) != null) {
                logger.warn("注册失败：用户名已存在 - {}", user.getUsername());
                return false;
            }

            // 检查邮箱是否已被使用
            if (user.getEmail() != null && userMapper.findByEmail(user.getEmail()) != null) {
                logger.warn("注册失败：邮箱已被使用 - {}", user.getEmail());
                return false;
            }

            // 检查手机号是否已被使用
            if (user.getPhone() != null && userMapper.findByPhone(user.getPhone()) != null) {
                logger.warn("注册失败：手机号已被使用 - {}", user.getPhone());
                return false;
            }

            // 密码加密
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);

            // 设置默认状态为启用
            if (user.getStatus() == null) {
                user.setStatus(1);
            }

            // 设置创建时间
            if (user.getCreatedAt() == null) {
                user.setCreatedAt(LocalDateTime.now());
            }

            // 插入数据库
            int result = userMapper.insert(user);
            
            if (result > 0) {
                logger.info("用户注册成功 - 用户名: {}", user.getUsername());
                return true;
            } else {
                logger.warn("用户注册失败 - 用户名: {}", user.getUsername());
                return false;
            }
        } catch (Exception e) {
            logger.error("用户注册异常 - 用户名: {}, 错误: {}", user.getUsername(), e.getMessage());
            return false;
        }
    }

    /**
     * 验证用户登录
     * <p>
     * 该方法会：
     * 1. 根据用户名查询用户
     * 2. 验证密码是否正确
     * 3. 检查用户状态是否正常
     * </p>
     *
     * @param username 用户名
     * @param password 密码（明文）
     * @return 验证成功返回用户对象，失败返回 null
     */
    public User login(String username, String password) {
        try {
            // 查询用户
            User user = userMapper.findByUsername(username);
            
            if (user == null) {
                logger.warn("登录失败：用户不存在 - {}", username);
                return null;
            }

            // 验证密码
            if (!passwordEncoder.matches(password, user.getPassword())) {
                logger.warn("登录失败：密码错误 - {}", username);
                return null;
            }

            // 检查用户状态
            if (user.getStatus() != null && user.getStatus() == 0) {
                logger.warn("登录失败：用户已被禁用 - {}", username);
                return null;
            }

            logger.info("用户登录成功 - {}", username);
            return user;
        } catch (Exception e) {
            logger.error("用户登录异常 - 用户名: {}, 错误: {}", username, e.getMessage());
            return null;
        }
    }

    /**
     * 更新用户信息
     *
     * @param user 用户对象（需包含 id）
     * @return 更新成功返回 true，失败返回 false
     */
    public boolean update(User user) {
        try {
            if (user.getId() == null) {
                logger.warn("更新用户失败：用户ID为空");
                return false;
            }

            // 检查用户是否存在
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
        } catch (Exception e) {
            logger.error("用户信息更新异常 - ID: {}, 错误: {}", user.getId(), e.getMessage());
            return false;
        }
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 删除成功返回 true，失败返回 false
     */
    public boolean deleteById(Long id) {
        try {
            if (id == null) {
                logger.warn("删除用户失败：用户ID为空");
                return false;
            }

            // 检查用户是否存在
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
     * @param id     用户ID
     * @param status 新状态（1-启用，0-禁用）
     * @return 更新成功返回 true，失败返回 false
     */
    public boolean updateStatus(Long id, Integer status) {
        try {
            if (id == null || status == null) {
                logger.warn("更新用户状态失败：参数为空 - ID: {}, Status: {}", id, status);
                return false;
            }

            // 检查用户是否存在
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
     * @param id          用户ID
     * @param newPassword 新密码（明文）
     * @return 修改成功返回 true，失败返回 false
     */
    public boolean changePassword(Long id, String newPassword) {
        try {
            if (id == null || newPassword == null || newPassword.isEmpty()) {
                logger.warn("修改密码失败：参数无效 - ID: {}", id);
                return false;
            }

            // 检查用户是否存在
            User existingUser = userMapper.findById(id);
            if (existingUser == null) {
                logger.warn("修改密码失败：用户不存在 - ID: {}", id);
                return false;
            }

            // 加密新密码
            String encodedPassword = passwordEncoder.encode(newPassword);

            // 创建临时用户对象用于更新
            User updateUser = new User();
            updateUser.setId(id);
            updateUser.setPassword(encodedPassword);

            int result = userMapper.update(updateUser);
            
            if (result > 0) {
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
     * 根据用户名删除用户
     *
     * @param username 用户名
     * @return 删除成功返回 true，失败返回 false
     */
    public boolean deleteByUsername(String username) {
        try {
            if (username == null || username.isEmpty()) {
                logger.warn("删除用户失败：用户名为空");
                return false;
            }

            // 检查用户是否存在
            User existingUser = userMapper.findByUsername(username);
            if (existingUser == null) {
                logger.warn("删除用户失败：用户不存在 - 用户名: {}", username);
                return false;
            }

            int result = userMapper.deleteById(existingUser.getId());
            
            if (result > 0) {
                logger.info("用户删除成功 - 用户名: {}", username);
                return true;
            } else {
                logger.warn("用户删除失败 - 用户名: {}", username);
                return false;
            }
        } catch (Exception e) {
            logger.error("用户删除异常 - 用户名: {}, 错误: {}", username, e.getMessage());
            return false;
        }
    }
}
