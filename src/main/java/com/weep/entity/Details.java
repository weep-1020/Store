package com.weep.entity;

import java.time.LocalDateTime;

/**
 * 商品分类实体类
 * <p>
 * 用于表示系统中的商品分类信息
 * </p>
 *
 * @author Store Team
 * @version 1.0
 */
public class Details {

    /**
     * 分类ID
     */
    private Long id;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 父分类ID（0表示顶级分类）
     */
    private Long parentId;

    /**
     * 分类描述
     */
    private String description;

    /**
     * 分类图标URL
     */
    private String iconUrl;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 分类状态（1-启用，0-禁用）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 默认构造函数
     */
    public Details() {
    }

    /**
     * 带参数的构造函数
     *
     * @param id            分类ID
     * @param categoryName  分类名称
     * @param parentId      父分类ID
     * @param description   分类描述
     * @param iconUrl       分类图标URL
     * @param sortOrder     排序顺序
     * @param status        分类状态
     * @param createdAt     创建时间
     * @param updatedAt     更新时间
     */
    public Details(Long id, String categoryName, Long parentId, String description,
                   String iconUrl, Integer sortOrder, Integer status,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.categoryName = categoryName;
        this.parentId = parentId;
        this.description = description;
        this.iconUrl = iconUrl;
        this.sortOrder = sortOrder;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Details{" +
                "id=" + id +
                ", categoryName='" + categoryName + '\'' +
                ", parentId=" + parentId +
                ", description='" + description + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", sortOrder=" + sortOrder +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
