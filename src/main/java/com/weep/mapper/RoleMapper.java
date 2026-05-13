package com.weep.mapper;

import com.weep.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleMapper {

    List<String> findRoleCodesByUserId(@Param("userId") Long userId);
    List<String> findRoleCodesByUsername(@Param("username") String username);
    List<String> findRoleCodesByPhone(@Param("phone") String phone);

    List<Role> findAll();
    Role findById(@Param("id") Long id);
    Role findByRoleCode(@Param("roleCode") String roleCode);
    int insert(Role role);
    int update(Role role);
    int deleteById(@Param("id") Long id);

    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
    int deleteUserRoles(@Param("userId") Long userId);
    List<Long> findRoleIdsByUserId(@Param("userId") Long userId);
    int deleteUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
}
