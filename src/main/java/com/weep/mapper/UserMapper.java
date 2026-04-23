package com.weep.mapper;

import com.weep.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户数据访问接口
 * <p>
 * 提供用户相关的数据库操作方法
 * </p>
 *
 * @author Store Team
 * @version 1.0
 */
@Mapper
public interface UserMapper {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户对象，如果不存在则返回 null
     */
    User findByUsername(@Param("username") String username);

    /**
     * 根据用户ID查询用户
     *
     * @param id 用户ID
     * @return 用户对象，如果不存在则返回 null
     */
    User findById(@Param("id") Long id);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱地址
     * @return 用户对象，如果不存在则返回 null
     */
    User findByEmail(@Param("email") String email);

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户对象，如果不存在则返回 null
     */
    User findByPhone(@Param("phone") String phone);

    /**
     * 查询所有用户
     *
     * @return 用户列表
     */
    List<User> findAll();

    /**
     * 插入新用户
     *
     * @param user 用户对象
     * @return 影响的行数
     */
    int insert(User user);

    /**
     * 更新用户信息
     *
     * @param user 用户对象
     * @return 影响的行数
     */
    int update(User user);

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 影响的行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 更新用户状态
     *
     * @param id     用户ID
     * @param status 新状态
     * @return 影响的行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
