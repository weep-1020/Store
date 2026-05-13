package com.weep.mapper;

import com.weep.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PermissionMapper {

    List<String> findPermissionCodesByUserId(@Param("userId") Long userId);
    List<String> findPermissionCodesByUsername(@Param("username") String username);
    List<String> findPermissionCodesByPhone(@Param("phone") String phone);

    List<Permission> findAll();
    Permission findById(@Param("id") Long id);
    Permission findByPermissionCode(@Param("permissionCode") String permissionCode);
    int insert(Permission permission);
    int update(Permission permission);
    int deleteById(@Param("id") Long id);

    int insertRolePermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);
    int deleteRolePermissions(@Param("roleId") Long roleId);
    List<Long> findPermissionIdsByRoleId(@Param("roleId") Long roleId);
}
