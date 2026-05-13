package com.weep.mapper;

import com.weep.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单明细表数据访问接口
 */
@Mapper
public interface OrderItemMapper {

    /**
     * 根据ID查询订单明细
     * @param id 主键ID
     * @return 订单明细对象
     */
    OrderItem findById(@Param("id") Long id);

    /**
     * 根据订单编号查询订单明细列表
     * @param order_no 订单编号
     * @return 订单明细列表
     */
    List<OrderItem> findByOrderNo(@Param("order_no") String order_no);

    /**
     * 查询所有订单明细
     * @return 订单明细列表
     */
    List<OrderItem> findAll();

    /**
     * 插入订单明细
     * @param orderItem 订单明细对象
     * @return 影响行数
     */
    int insert(OrderItem orderItem);

    /**
     * 更新订单明细
     * @param orderItem 订单明细对象
     * @return 影响行数
     */
    int update(OrderItem orderItem);

    /**
     * 根据ID删除订单明细
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据订单编号删除订单明细
     * @param order_no 订单编号
     * @return 影响行数
     */
    int deleteByOrderNo(@Param("order_no") String order_no);

    /**
     * 批量插入订单明细
     * @param orderItems 订单明细列表
     * @return 影响行数
     */
    int batchInsert(@Param("orderItems") List<OrderItem> orderItems);
}