package com.weep.service;

import com.weep.entity.Order;

import java.util.List;
import java.util.Map;

/**
 * 订单服务接口
 * 
 * <p>提供订单管理的核心业务逻辑，包括订单查询、创建、支付、发货、完成、取消等全生命周期管理。
 * 所有写操作都使用 @Transactional 保证事务一致性。
 * 
 * <p>主要功能：
 * <ul>
 *   <li>订单查询（按订单号、用户ID、状态等）</li>
 *   <li>订单创建（直接创建或从购物车转换）</li>
 *   <li>订单状态流转（支付、发货、完成、取消）</li>
 *   <li>库存管理（下单扣减、取消恢复）</li>
 * </ul>
 */
public interface OrderService {

    /**
     * 根据订单号查询订单
     * 
     * @param order_no 订单编号
     * @return 订单对象，不存在则返回null
     */
    Order findByOrderNo(String order_no);

    /**
     * 根据用户ID查询订单列表
     * 
     * @param user_id 用户ID
     * @return 订单列表，无数据则返回空列表
     */
    List<Order> findByUserId(Long user_id);

    /**
     * 根据订单状态查询订单列表
     * 
     * @param status 订单状态（0-待支付，1-已支付，2-已发货，3-已完成，4-已取消）
     * @return 订单列表
     */
    List<Order> findByStatus(String status);

    /**
     * 根据用户ID和订单状态查询订单列表
     * 
     * @param user_id 用户ID
     * @param status 订单状态
     * @return 订单列表
     */
    List<Order> findByUserIdAndStatus(Long user_id, String status);

    /**
     * 查询所有订单（管理员专用）
     * 
     * @return 所有订单列表
     */
    List<Order> findAll();

    /**
     * 创建订单
     * 
     * <p>直接传入订单对象创建，适用于特殊场景。通常使用 createOrderFromCart 方法。
     * 
     * @param order 订单对象
     * @return 创建成功返回true
     */
    boolean createOrder(Order order);

    /**
     * 从购物车创建订单（核心业务方法）
     * 
     * <p>这是电商系统最核心的业务流程，执行以下操作：
     * <ol>
     *   <li>查询用户的购物车项</li>
     *   <li>校验商品存在性和库存充足性</li>
     *   <li>计算订单总金额</li>
     *   <li>生成订单号并创建订单主表记录</li>
     *   <li>批量插入订单明细</li>
     *   <li>原子扣减商品库存（防止超卖）</li>
     *   <li>清空购物车</li>
     * </ol>
     * 
     * <p>整个流程在事务中执行，任何步骤失败都会回滚。
     * 
     * @param user_id 用户ID
     * @param cartItemIds 选中的购物车项ID列表（为空则转换全部购物车）
     * @param receiverInfo 收货信息Map，包含receiver_name、receiver_phone、receiver_address、remark
     * @return 订单号
     * @throws RuntimeException 当购物车为空、商品不存在、库存不足等情况时抛出
     */
    String createOrderFromCart(Long user_id, List<Long> cartItemIds, Map<String, String> receiverInfo);

    /**
     * 更新订单信息
     * 
     * @param order 订单对象（必须包含order_no）
     * @return 更新成功返回true
     */
    boolean updateOrder(Order order);

    /**
     * 删除订单（同时删除订单明细）
     * 
     * @param order_no 订单编号
     * @return 删除成功返回true
     */
    boolean deleteByOrderNo(String order_no);

    /**
     * 更新订单状态
     * 
     * @param order_no 订单编号
     * @param status 新状态
     * @return 更新成功返回true
     */
    boolean updateStatus(String order_no, String status);

    /**
     * 支付订单
     * 
     * <p>验证订单状态为待支付，检查支付金额是否充足，更新支付信息和状态。
     * 
     * @param order_no 订单编号
     * @param payment_method 支付方式（wechat/alipay/bank）
     * @param pay_amount 支付金额
     * @return 支付成功返回true
     */
    boolean payOrder(String order_no, String payment_method, Double pay_amount);

    /**
     * 发货订单
     * 
     * <p>验证订单状态为已支付，记录发货时间，更新状态为已发货。
     * 
     * @param order_no 订单编号
     * @return 发货成功返回true
     */
    boolean deliverOrder(String order_no);

    /**
     * 完成订单
     * 
     * <p>验证订单状态为已发货，更新状态为已完成。
     * 
     * @param order_no 订单编号
     * @return 完成成功返回true
     */
    boolean completeOrder(String order_no);

    /**
     * 取消订单
     * 
     * <p>验证订单状态允许取消（已完成和已取消的订单不能取消）。
     * 如果订单已支付或已发货，会恢复商品库存。
     * 
     * @param order_no 订单编号
     * @return 取消成功返回true
     */
    boolean cancelOrder(String order_no);
}
