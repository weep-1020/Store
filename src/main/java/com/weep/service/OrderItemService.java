package com.weep.service;

import com.weep.entity.OrderItem;

import java.util.List;

/**
 * 订单明细服务接口
 */
public interface OrderItemService {

    /**
     * 根据ID查询订单明细
     * @param id 主键ID
     * @return 订单明细对象
     */
    OrderItem findById(Long id);

    /**
     * 根据订单编号查询订单明细列表
     * @param order_no 订单编号
     * @return 订单明细列表
     */
    List<OrderItem> findByOrderNo(String order_no);

    /**
     * 查询所有订单明细
     * @return 订单明细列表
     */
    List<OrderItem> findAll();

    /**
     * 添加订单明细
     * @param orderItem 订单明细对象
     * @return 添加成功返回true
     */
    boolean addOrderItem(OrderItem orderItem);

    /**
     * 更新订单明细
     * @param orderItem 订单明细对象
     * @return 更新成功返回true
     */
    boolean updateOrderItem(OrderItem orderItem);

    /**
     * 根据ID删除订单明细
     * @param id 主键ID
     * @return 删除成功返回true
     */
    boolean deleteById(Long id);

    /**
     * 根据订单编号删除订单明细
     * @param order_no 订单编号
     * @return 删除成功返回true
     */
    boolean deleteByOrderNo(String order_no);

    /**
     * 批量添加订单明细
     * @param orderItems 订单明细列表
     * @return 添加成功返回true
     */
    boolean batchAddOrderItems(List<OrderItem> orderItems);
}