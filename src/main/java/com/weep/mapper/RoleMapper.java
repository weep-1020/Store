package com.weep.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色 Mapper 接口
 * <p>
 * 提供用户角色的查询操作
 * </p>
 *
 * @author Store Team
 * @version 1.0
 */
@Mapper
public interface RoleMapper {

    /**
     * 根据用户ID查询角色代码列表
     *
     * @param userId 用户ID
     * @return 角色代码列表（如：SUPER_ADMIN, MERCHANT, USER）
     */
    List<String> findRoleCodesByUserId(@Param("userId") Long userId);

    /**
     * 根据用户名查询角色代码列表
     *
     * @param username 用户名
     * @return 角色代码列表
     */
    List<String> findRoleCodesByUsername(@Param("username") String username);
}
