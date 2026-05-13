package com.weep.mapper;

import com.weep.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单表数据访问接口（MyBatis Mapper）
 * 
 * <p>提供订单表的 CRUD 操作，SQL 语句在 XML 文件中定义。
 */
@Mapper
public interface OrderMapper {

    /**
     * 根据订单号查询订单
     * 
     * @param order_no 订单编号
     * @return 订单对象，不存在则返回null
     */
    Order findByOrderNo(@Param("order_no") String order_no);

    /**
     * 根据用户ID查询订单列表
     * 
     * @param user_id 用户ID
     * @return 订单列表
     */
    List<Order> findByUserId(@Param("user_id") Long user_id);

    /**
     * 根据订单状态查询订单列表
     * 
     * @param status 订单状态
     * @return 订单列表
     */
    List<Order> findByStatus(@Param("status") String status);

    /**
     * 根据用户ID和订单状态查询订单列表
     * 
     * @param user_id 用户ID
     * @param status 订单状态
     * @return 订单列表
     */
    List<Order> findByUserIdAndStatus(@Param("user_id") Long user_id, @Param("status") String status);

    /**
     * 查询所有订单
     * 
     * @return 订单列表
     */
    List<Order> findAll();

    /**
     * 插入订单记录
     * 
     * @param order 订单对象
     * @return 影响行数
     */
    int insert(Order order);

    /**
     * 更新订单信息
     * 
     * @param order 订单对象（必须包含order_no）
     * @return 影响行数
     */
    int update(Order order);

    /**
     * 根据订单号删除订单
     * 
     * @param order_no 订单编号
     * @return 影响行数
     */
    int deleteByOrderNo(@Param("order_no") String order_no);

    /**
     * 更新订单状态
     * 
     * @param order_no 订单编号
     * @param status 新状态
     * @return 影响行数
     */
    int updateStatus(@Param("order_no") String order_no, @Param("status") String status);

    /**
     * 更新订单支付信息
     * 
     * <p>同时更新支付方式、支付金额、支付时间和状态。
     * 
     * @param order_no 订单编号
     * @param payment_method 支付方式
     * @param pay_amount 支付金额
     * @return 影响行数
     */
    int updatePayment(@Param("order_no") String order_no,
                      @Param("payment_method") String payment_method,
                      @Param("pay_amount") Double pay_amount);

    /**
     * 更新订单发货时间
     * 
     * <p>同时更新状态为已发货。
     * 
     * @param order_no 订单编号
     * @param delivery_time 发货时间
     * @return 影响行数
     */
    int updateDeliveryTime(@Param("order_no") String order_no, @Param("delivery_time") java.util.Date delivery_time);
}
