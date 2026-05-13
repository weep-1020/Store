package com.weep.entity;

import java.util.Date;

/**
 * 购物车项实体类
 */
public class CartItem {
    private Long id;              // 主键ID
    private Long user_id;         // 用户ID
    private String sku_id;        // 商品SKU ID
    private String product_name;  // 商品名称
    private String product_image; // 商品图片
    private Double product_price; // 商品单价
    private String product_specs; // 商品规格
    private Integer quantity;     // 数量
    private Date created_at;      // 创建时间
    private Date updated_at;      // 更新时间

    public CartItem() {
    }

    public CartItem(Long id, Long user_id, String sku_id, String product_name,
                    String product_image, Double product_price, String product_specs,
                    Integer quantity, Date created_at, Date updated_at) {
        this.id = id;
        this.user_id = user_id;
        this.sku_id = sku_id;
        this.product_name = product_name;
        this.product_image = product_image;
        this.product_price = product_price;
        this.product_specs = product_specs;
        this.quantity = quantity;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUser_id() {
        return user_id;
    }

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }

    public String getSku_id() {
        return sku_id;
    }

    public void setSku_id(String sku_id) {
        this.sku_id = sku_id;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public String getProduct_image() {
        return product_image;
    }

    public void setProduct_image(String product_image) {
        this.product_image = product_image;
    }

    public Double getProduct_price() {
        return product_price;
    }

    public void setProduct_price(Double product_price) {
        this.product_price = product_price;
    }

    public String getProduct_specs() {
        return product_specs;
    }

    public void setProduct_specs(String product_specs) {
        this.product_specs = product_specs;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public Date getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(Date updated_at) {
        this.updated_at = updated_at;
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "id=" + id +
                ", user_id=" + user_id +
                ", sku_id='" + sku_id + '\'' +
                ", product_name='" + product_name + '\'' +
                ", product_image='" + product_image + '\'' +
                ", product_price=" + product_price +
                ", product_specs='" + product_specs + '\'' +
                ", quantity=" + quantity +
                ", created_at=" + created_at +
                ", updated_at=" + updated_at +
                '}';
    }
}