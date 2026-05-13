package com.weep.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/*
 * 自定义认证提供者
 * 使用 UserDetailsService 加载用户信息并验证密码
 * 符合 Spring Security 标准的认证流程
 */
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    /* 日志记录器 */
    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationProvider.class);

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /*
     * 执行认证逻辑
     * 1. 使用 UserDetailsService 加载用户信息
     * 2. 验证密码
     * 3. 检查用户状态
     * 4. 返回包含权限的 Authentication 对象
     *
     * @param authentication 包含用户名和密码的认证对象
     * @return 认证成功后的 Authentication 对象（包含权限）
     * @throws AuthenticationException 认证失败时抛出异常
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = (String) authentication.getCredentials();

        logger.debug("开始认证用户: {}", username);

        /* 1. 使用 UserDetailsService 加载用户详情 */
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        /* 2. 验证密码 */
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            logger.warn("认证失败：密码错误 - {}", username);
            throw new BadCredentialsException("用户名或密码错误");
        }

        /* 3. 检查用户是否启用 */
        if (!userDetails.isEnabled()) {
            logger.warn("认证失败：用户已被禁用 - {}", username);
            throw new DisabledException("用户账号已被禁用，请联系管理员");
        }

        logger.info("认证成功 - 用户: {}, 权限: {}", username, userDetails.getAuthorities());

        /* 4. 返回认证成功的 Authentication 对象 */
        return new UsernamePasswordAuthenticationToken(
                userDetails,      /* Principal（主体）- UserDetails 对象 */
                null,             /* Credentials（凭证）- 认证成功后清除 */
                userDetails.getAuthorities()  /* Authorities（权限列表） */
        );
    }
    /*
     * 判断是否支持该认证类型
     * 只支持 UsernamePasswordAuthenticationToken 类型的认证
     *
     * @param authentication 认证类型
     * @return 是否支持
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
