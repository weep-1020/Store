package com.weep.entity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户实体类
 * <p>
 * 用于表示系统中的用户信息
 * </p>
 *
 * @author Store Team
 * @version 1.0
 */
public class User {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（加密存储）
     */
    private String password;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 用户头像图片ID（存储图片路径或ID）
     */
    private String pic;

    /**
     * 用户状态（1-启用，0-禁用）
     */
    private Integer status;

    /**
     * 角色ID（FK -> roles.id）
     */
    private Long roleId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 用户角色列表（非数据库字段）
     */
    private List<String> roles;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginAt;

    /**
     * 用户权限列表（非数据库字段）
     */
    private List<String> permissions;

    /**
     * 默认构造函数
     */
    public User() {
    }

    /**
     * 带参数的构造函数
     *
     * @param id        用户ID
     * @param username  用户名
     * @param password  密码
     * @param email     邮箱
     * @param phone     手机号
     * @param status    用户状态
     * @param createdAt 创建时间
     */
    public User(Long id, String username, String password, String email, String phone, Integer status, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.status = status;
        this.createdAt = createdAt;
    }

    public User(Long id, String username, String password,
                String email, String phone, String pic,
                Integer status, LocalDateTime createdAt, List<String> roles,
                List<String> permissions) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.pic = pic;
        this.status = status;
        this.createdAt = createdAt;
        this.roles = roles;
        this.permissions = permissions;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    /**
     * 获取用户头像图片ID
     * @return 图片ID或路径
     */
    public String getPic() {
        return pic;
    }

    /**
     * 设置用户头像图片ID
     * @param pic 图片ID或路径
     */
    public void setPic(String pic) {
        this.pic = pic;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='***'" +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", pic=" + pic +
                ", status=" + status +
                ", roleId=" + roleId +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", lastLoginAt=" + lastLoginAt +
                ", roles=" + roles +
                ", permissions=" + permissions +
                '}';
    }
}
