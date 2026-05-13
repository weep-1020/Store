package com.weep.entity;

import java.util.Date;

/**
 * 订单明细表实体类
 * 
 * <p>记录订单中每个商品的详细信息，包括商品名称、价格、数量等。
 * 一个订单可以包含多个订单明细（一对多关系）。
 * 
 * <p>主要作用：
 * <ul>
 *   <li>保存下单时的商品信息快照（防止商品信息修改后影响历史订单）</li>
 *   <li>记录实际成交价格（可能与当前商品价格不同）</li>
 *   <li>支持订单退款、售后等业务的精确计算</li>
 * </ul>
 */
public class OrderItem {
    private Long id;              // 主键ID（自增）
    private String order_no;      // 订单编号（关联订单表，外键）
    private String sku_id;        // 商品SKU ID（关联商品表）
    private String product_name;  // 商品名称（下单时的快照）
    private String product_image; // 商品图片URL或ID（下单时的快照）
    private Double product_price; // 商品单价（下单时的实际成交价）
    private String product_specs; // 商品规格（如颜色、尺寸等）
    private Integer quantity;     // 购买数量
    private Double subtotal;      // 小计金额（product_price * quantity）
    private Date created_at;      // 创建时间
    private Date updated_at;      // 更新时间

    public OrderItem() {
    }

    public OrderItem(Long id, String order_no, String sku_id, String product_name,
                     String product_image, Double product_price, String product_specs,
                     Integer quantity, Double subtotal, Date created_at, Date updated_at) {
        this.id = id;
        this.order_no = order_no;
        this.sku_id = sku_id;
        this.product_name = product_name;
        this.product_image = product_image;
        this.product_price = product_price;
        this.product_specs = product_specs;
        this.quantity = quantity;
        this.subtotal = subtotal;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrder_no() {
        return order_no;
    }

    public void setOrder_no(String order_no) {
        this.order_no = order_no;
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

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
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
        return "OrderItem{" +
                "id=" + id +
                ", order_no='" + order_no + '\'' +
                ", sku_id='" + sku_id + '\'' +
                ", product_name='" + product_name + '\'' +
                ", product_image='" + product_image + '\'' +
                ", product_price=" + product_price +
                ", product_specs='" + product_specs + '\'' +
                ", quantity=" + quantity +
                ", subtotal=" + subtotal +
                ", created_at=" + created_at +
                ", updated_at=" + updated_at +
                '}';
    }
}