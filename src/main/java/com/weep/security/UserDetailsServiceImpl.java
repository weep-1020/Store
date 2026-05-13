package com.weep.security;

import com.weep.entity.User;
import com.weep.mapper.PermissionMapper;
import com.weep.mapper.RoleMapper;
import com.weep.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/*
 * 自定义用户详情服务实现
 * 负责从数据库加载用户信息和权限，供 Spring Security 使用
 * 符合 Spring Security 标准的 UserDetailsService 接口
 * 使用手机号作为用户唯一标识
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    /* 日志记录器 */
    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    UserService userService;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    /*
     * 根据用户名加载用户详情
     * Spring Security 在认证时会自动调用此方法
     *
     * @param username 用户名
     * @return UserDetails 包含用户信息和权限的对象
     * @throws UsernameNotFoundException 用户不存在时抛出异常
     */
    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        logger.debug("加载用户详情 - 手机号: {}", phone);

        /* 1. 根据手机号查询用户信息 */
        User user = userService.findByPhone(phone);
        
        if (user == null) {
            logger.warn("用户不存在 - 手机号: {}", phone);
            throw new UsernameNotFoundException("用户不存在: " + phone);
        }

        /* 2. 检查用户状态 */
        if (user.getStatus() != null && user.getStatus() == 0) {
            logger.warn("用户已被禁用 - 手机号: {}, 用户名: {}", phone, user.getUsername());
            throw new DisabledException("用户账号已被禁用: " + phone);
        }

        /* 3. 查询用户的角色和权限（通过手机号） */
        List<String> roles = roleMapper.findRoleCodesByPhone(phone);
        List<String> permissions = permissionMapper.findPermissionCodesByPhone(phone);

        logger.info("加载用户权限 - 手机号: {}, 用户名: {}, 角色: {}, 权限: {}", phone, user.getUsername(), roles, permissions);

        /* 4. 构建权限列表 */
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        /* 添加角色（前缀 ROLE_ 是 Spring Security 规范） */
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }
        
        /* 添加权限 */
        for (String permission : permissions) {
            authorities.add(new SimpleGrantedAuthority(permission));
        }

        /* 5. 返回 Spring Security 标准的 UserDetails 对象 */
        return new org.springframework.security.core.userdetails.User(
                user.getPhone(),         /* 使用手机号作为主体标识 */
                user.getPassword(),      /* 密码（已加密） */
                user.getStatus() != null && user.getStatus() == 1,  /* 是否启用 */
                true,                    /* 是否账户未过期 */
                true,                    /* 是否凭证未过期 */
                true,                    /* 是否账户未锁定 */
                authorities              /* 权限列表 */
        );
    }
}
