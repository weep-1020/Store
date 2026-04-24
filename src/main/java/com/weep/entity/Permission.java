package com.weep.entity;

/**
 * 权限实体类
 * <p>
 * 用于表示系统中的权限资源
 * </p>
 *
 * @author Store Team
 * @version 1.0
 */
public class Permission {

    /**
     * 权限ID
     */
    private Long id;

    /**
     * 权限名称
     */
    private String permissionName;

    /**
     * 权限代码（唯一标识）
     */
    private String permissionCode;

    /**
     * 资源类型（api、menu等）
     */
    private String resourceType;

    public Permission() {
    }

    public Permission(Long id, String permissionName, String permissionCode, String resourceType) {
        this.id = id;
        this.permissionName = permissionName;
        this.permissionCode = permissionCode;
        this.resourceType = resourceType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    public String getPermissionCode() {
        return permissionCode;
    }

    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    @Override
    public String toString() {
        return "Permission{" +
                "id=" + id +
                ", permissionName='" + permissionName + '\'' +
                ", permissionCode='" + permissionCode + '\'' +
                ", resourceType='" + resourceType + '\'' +
                '}';
    }
}
