package com.weep.entity;

import java.util.Date;

/**
 * 订单主表实体类
 * 
 * <p>记录订单的基本信息，包括金额、状态、支付方式、收货信息等。
 * 是电商系统的核心数据模型，贯穿用户下单、支付、发货、完成的整个流程。
 * 
 * <p>订单状态流转：
 * <ul>
 *   <li>0 - 待支付 (PENDING): 订单创建后的初始状态</li>
 *   <li>1 - 已支付 (PAID): 用户完成支付</li>
 *   <li>2 - 已发货 (DELIVERED): 商家发货</li>
 *   <li>3 - 已完成 (COMPLETED): 用户确认收货</li>
 *   <li>4 - 已取消 (CANCELLED): 订单被取消</li>
 * </ul>
 * 
 * <p>支付方式：
 * <ul>
 *   <li>wechat - 微信支付</li>
 *   <li>alipay - 支付宝</li>
 *   <li>bank - 银行转账</li>
 * </ul>
 */
public class Order {
    private Long id;                  // 主键ID（自增）
    private String order_no;          // 订单编号（唯一标识，格式：YYYYMMDDHHmmss+毫秒+序号）
    private Long user_id;             // 用户ID（关联用户表）
    private Double total_amount;      // 订单总金额（商品小计之和）
    private Double pay_amount;        // 实际支付金额（可能因优惠而小于总金额）
    private String status;            // 订单状态：0-待支付，1-已支付，2-已发货，3-已完成，4-已取消
    private String payment_method;    // 支付方式：wechat-微信支付，alipay-支付宝，bank-银行转账
    private Date payment_time;        // 支付时间（状态变为已支付时记录）
    private Date delivery_time;       // 发货时间（状态变为已发货时记录）
    private String receiver_name;     // 收货人姓名
    private String receiver_phone;    // 收货人电话
    private String receiver_address;  // 收货地址（省市区详细地址）
    private String remark;            // 订单备注（用户留言）
    private Date created_at;          // 创建时间（下单时间）
    private Date updated_at;          // 更新时间（最后修改时间）

    public Order() {
    }

    public Order(Long id, String order_no, Long user_id, Double total_amount,
                 Double pay_amount, String status, String payment_method,
                 Date payment_time, Date delivery_time, String receiver_name,
                 String receiver_phone, String receiver_address, String remark,
                 Date created_at, Date updated_at) {
        this.id = id;
        this.order_no = order_no;
        this.user_id = user_id;
        this.total_amount = total_amount;
        this.pay_amount = pay_amount;
        this.status = status;
        this.payment_method = payment_method;
        this.payment_time = payment_time;
        this.delivery_time = delivery_time;
        this.receiver_name = receiver_name;
        this.receiver_phone = receiver_phone;
        this.receiver_address = receiver_address;
        this.remark = remark;
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

    public String getOrder_no() {
        return order_no;
    }

    public void setOrder_no(String order_no) {
        this.order_no = order_no;
    }

    public Long getUser_id() {
        return user_id;
    }

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }

    public Double getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(Double total_amount) {
        this.total_amount = total_amount;
    }

    public Double getPay_amount() {
        return pay_amount;
    }

    public void setPay_amount(Double pay_amount) {
        this.pay_amount = pay_amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPayment_method() {
        return payment_method;
    }

    public void setPayment_method(String payment_method) {
        this.payment_method = payment_method;
    }

    public Date getPayment_time() {
        return payment_time;
    }

    public void setPayment_time(Date payment_time) {
        this.payment_time = payment_time;
    }

    public Date getDelivery_time() {
        return delivery_time;
    }

    public void setDelivery_time(Date delivery_time) {
        this.delivery_time = delivery_time;
    }

    public String getReceiver_name() {
        return receiver_name;
    }

    public void setReceiver_name(String receiver_name) {
        this.receiver_name = receiver_name;
    }

    public String getReceiver_phone() {
        return receiver_phone;
    }

    public void setReceiver_phone(String receiver_phone) {
        this.receiver_phone = receiver_phone;
    }

    public String getReceiver_address() {
        return receiver_address;
    }

    public void setReceiver_address(String receiver_address) {
        this.receiver_address = receiver_address;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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
        return "Order{" +
                "id=" + id +
                ", order_no='" + order_no + '\'' +
                ", user_id=" + user_id +
                ", total_amount=" + total_amount +
                ", pay_amount=" + pay_amount +
                ", status='" + status + '\'' +
                ", payment_method='" + payment_method + '\'' +
                ", payment_time=" + payment_time +
                ", delivery_time=" + delivery_time +
                ", receiver_name='" + receiver_name + '\'' +
                ", receiver_phone='" + receiver_phone + '\'' +
                ", receiver_address='" + receiver_address + '\'' +
                ", remark='" + remark + '\'' +
                ", created_at=" + created_at +
                ", updated_at=" + updated_at +
                '}';
    }
}