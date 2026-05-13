package com.weep.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类
 * <p>
 * 用于表示系统中的商品信息
 * </p>
 *
 * @author Store Team
 * @version 1.0
 */
public class Products {

    /**
     * 商品编号
     */
    private String pid;

    /**
     * 商品名称
     */
    private String pName;

    /**
     * 商品价格
     */
    private BigDecimal price;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 商品分类
     */
    private String category;

    /**
     * 商品状态（1-上架，0-下架）
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
    public Products() {
    }

    /**
     * 带参数的构造函数
     *
     * @param pid         商品编号
     * @param pName       商品名称
     * @param price       商品价格
     * @param description 商品描述
     * @param stock       库存数量
     * @param category    商品分类
     * @param status      商品状态
     * @param createdAt   创建时间
     * @param updatedAt   更新时间
     */
    public Products(String pid, String pName, BigDecimal price, String description, 
                    Integer stock, String category, Integer status, 
                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.pid = pid;
        this.pName = pName;
        this.price = price;
        this.description = description;
        this.stock = stock;
        this.category = category;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getpName() {
        return pName;
    }

    public void setpName(String pName) {
        this.pName = pName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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
        return "Products{" +
                "pid='" + pid + '\'' +
                ", pName='" + pName + '\'' +
                ", price=" + price +
                ", description='" + description + '\'' +
                ", stock=" + stock +
                ", category='" + category + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
