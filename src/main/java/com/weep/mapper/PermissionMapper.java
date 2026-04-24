package com.weep.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 权限 Mapper 接口
 * <p>
 * 提供用户权限的查询操作
 * </p>
 *
 * @author Store Team
 * @version 1.0
 */
@Mapper
public interface PermissionMapper {

    /**
     * 根据用户ID查询权限代码列表
     *
     * @param userId 用户ID
     * @return 权限代码列表（如：product:view, order:manage）
     */
    List<String> findPermissionCodesByUserId(@Param("userId") Long userId);

    /**
     * 根据用户名查询权限代码列表
     *
     * @param username 用户名
     * @return 权限代码列表
     */
    List<String> findPermissionCodesByUsername(@Param("username") String username);
}
