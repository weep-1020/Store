package com.weep.service.impl;

import com.weep.entity.CartItem;
import com.weep.entity.Order;
import com.weep.entity.OrderItem;
import com.weep.entity.Products;
import com.weep.mapper.CartItemMapper;
import com.weep.mapper.OrderItemMapper;
import com.weep.mapper.OrderMapper;
import com.weep.mapper.ProductsMapper;
import com.weep.service.OrderService;
import com.weep.util.IdGenerator;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 订单服务实现类
 *
 * <p>实现订单生命周期的完整管理，包括订单创建、支付、发货、完成、取消等操作。
 * 使用 @Transactional 注解保证事务一致性，确保订单数据的完整性和准确性。
 * 
 * <p>订单状态流转：
 * <pre>
 * 待支付(0) → 已支付(1) → 已发货(2) → 已完成(3)
 *        ↓           ↓
 *      已取消(4)   已取消(4)
 * </pre>
 */
@Service
public class OrderServiceImpl implements OrderService {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    /**
     * 订单Mapper接口
     */
    @Resource
    private OrderMapper orderMapper;

    /**
     * 订单明细Mapper接口
     */
    @Resource
    private OrderItemMapper orderItemMapper;

    /**
     * 购物车项Mapper接口
     */
    @Resource
    private CartItemMapper cartItemMapper;

    /**
     * 商品Mapper接口
     */
    @Resource
    private ProductsMapper productsMapper;

    // 订单状态常量
    private static final String STATUS_PENDING = "0";      // 待支付
    private static final String STATUS_PAID = "1";         // 已支付
    private static final String STATUS_DELIVERED = "2";    // 已发货
    private static final String STATUS_COMPLETED = "3";    // 已完成
    private static final String STATUS_CANCELLED = "4";    // 已取消

    /**
     * 订单号序号计数器（原子操作，保证并发安全）
     */
    private static final AtomicInteger seqCounter = new AtomicInteger(0);

    /**
     * 根据订单号查询订单
     *
     * @param order_no 订单编号
     * @return 订单对象，不存在则返回null
     */
    @Override
    public Order findByOrderNo(String order_no) {
        try {
            return orderMapper.findByOrderNo(order_no);
        } catch (Exception e) {
            logger.error("查询订单失败 - 订单号: {}, 错误: {}", order_no, e.getMessage());
            return null;
        }
    }

    /**
     * 根据用户ID查询订单列表
     *
     * @param user_id 用户ID
     * @return 用户的订单列表，无数据则返回空列表
     */
    @Override
    public List<Order> findByUserId(Long user_id) {
        try {
            return orderMapper.findByUserId(user_id);
        } catch (Exception e) {
            logger.error("查询订单失败 - 用户ID: {}, 错误: {}", user_id, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 根据状态查询订单列表
     *
     * @param status 订单状态
     * @return 对应状态的订单列表，无数据则返回空列表
     */
    @Override
    public List<Order> findByStatus(String status) {
        try {
            return orderMapper.findByStatus(status);
        } catch (Exception e) {
            logger.error("查询订单失败 - 状态: {}, 错误: {}", status, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 根据用户ID和状态查询订单列表
     *
     * @param user_id 用户ID
     * @param status 订单状态
     * @return 符合条件的订单列表，无数据则返回空列表
     */
    @Override
    public List<Order> findByUserIdAndStatus(Long user_id, String status) {
        try {
            return orderMapper.findByUserIdAndStatus(user_id, status);
        } catch (Exception e) {
            logger.error("查询订单失败 - 用户ID: {}, 状态: {}, 错误: {}", user_id, status, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 查询所有订单
     *
     * @return 订单列表，无数据则返回空列表
     */
    @Override
    public List<Order> findAll() {
        try {
            return orderMapper.findAll();
        } catch (Exception e) {
            logger.error("查询所有订单失败 - 错误: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 创建订单
     * 
     * <p>直接传入订单对象创建订单，适用于特殊场景。通常建议使用 createOrderFromCart 方法。
     * 如果订单号为空则自动生成，如果状态为空则默认为待支付状态。
     * 
     * @param order 订单对象，包含订单基本信息
     * @return 创建成功返回true，失败返回false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createOrder(Order order) {
        try {
            // 自动生成订单号（如果未提供）
            if (order.getOrder_no() == null || order.getOrder_no().isEmpty()) {
                order.setOrder_no(generateOrderNo());
            }
            // 设置默认状态为待支付
            if (order.getStatus() == null) {
                order.setStatus(STATUS_PENDING);
            }
            int result = orderMapper.insert(order);
            if (result > 0) {
                logger.info("订单创建成功 - 订单号: {}", order.getOrder_no());
                return true;
            } else {
                logger.warn("订单创建失败");
                return false;
            }
        } catch (Exception e) {
            logger.error("订单创建异常 - 错误: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从购物车创建订单（核心业务方法）
     * 
     * <p>这是电商系统最核心的业务流程，在事务中执行以下操作：
     * <ol>
     *   <li>查询用户的购物车项（全部或指定ID）</li>
     *   <li>校验商品存在性和库存充足性</li>
     *   <li>计算订单总金额和小计</li>
     *   <li>生成唯一订单号</li>
     *   <li>创建订单主表记录</li>
     *   <li>批量插入订单明细</li>
     *   <li>原子扣减商品库存（防止超卖）</li>
     *   <li>清空购物车</li>
     * </ol>
     * 
     * <p>任何步骤失败都会触发事务回滚，保证数据一致性。
     * 
     * @param user_id 用户ID
     * @param cartItemIds 选中的购物车项ID列表（为空则转换全部购物车）
     * @param receiverInfo 收货信息Map，包含receiver_name、receiver_phone、receiver_address、remark
     * @return 订单号
     * @throws RuntimeException 当购物车为空、商品不存在、库存不足等情况时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrderFromCart(Long user_id, List<Long> cartItemIds, Map<String, String> receiverInfo) {
        try {
            // ========== 步骤1: 查询购物车项 ==========
            List<CartItem> cartItems;
            if (cartItemIds == null || cartItemIds.isEmpty()) {
                // 未指定ID，查询用户的全部购物车
                cartItems = cartItemMapper.findByUserId(user_id);
            } else {
                // 根据ID列表查询指定的购物车项
                List<CartItem> allItems = cartItemMapper.findByIds(cartItemIds);
                // 过滤出属于当前用户的购物车项（安全校验）
                cartItems = new ArrayList<>();
                for (CartItem item : allItems) {
                    if (item != null && item.getUser_id().equals(user_id)) {
                        cartItems.add(item);
                    }
                }
            }

            // 校验购物车不为空
            if (cartItems.isEmpty()) {
                throw new RuntimeException("购物车为空");
            }

            // ========== 步骤2: 校验商品并计算金额 ==========
            double totalAmount = 0;
            List<OrderItem> orderItems = new ArrayList<>();

            for (CartItem cartItem : cartItems) {
                // 查询商品信息
                Products product = productsMapper.findBySpid(cartItem.getSku_id());
                if (product == null) {
                    throw new RuntimeException("商品不存在 - SKU: " + cartItem.getSku_id());
                }

                // 校验库存充足性
                if (product.getStock() != null && product.getStock() < cartItem.getQuantity()) {
                    throw new RuntimeException("库存不足 - SKU: " + cartItem.getSku_id()
                            + ", 库存: " + product.getStock() + ", 需求: " + cartItem.getQuantity());
                }

                // 计算小计并累加总金额
                double subtotal = cartItem.getProduct_price() * cartItem.getQuantity();
                totalAmount += subtotal;

                // 构建订单明细对象（保存下单时的商品快照）
                OrderItem orderItem = new OrderItem();
                orderItem.setSku_id(cartItem.getSku_id());
                orderItem.setProduct_name(cartItem.getProduct_name());
                orderItem.setProduct_image(cartItem.getProduct_image());
                orderItem.setProduct_price(cartItem.getProduct_price());
                orderItem.setProduct_specs(cartItem.getProduct_specs());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setSubtotal(subtotal);
                orderItems.add(orderItem);
            }

            // ========== 步骤3: 生成订单号并创建订单主表 ==========
            String orderNo = generateOrderNo();

            Order order = new Order();
            order.setOrder_no(orderNo);
            order.setUser_id(user_id);
            order.setTotal_amount(totalAmount);
            order.setPay_amount(0.0);  // 初始支付金额为0，支付后更新
            order.setStatus(STATUS_PENDING);  // 初始状态为待支付
            order.setReceiver_name(receiverInfo.get("receiver_name"));
            order.setReceiver_phone(receiverInfo.get("receiver_phone"));
            order.setReceiver_address(receiverInfo.get("receiver_address"));
            order.setRemark(receiverInfo.get("remark"));

            int orderResult = orderMapper.insert(order);
            if (orderResult <= 0) {
                throw new RuntimeException("订单主表创建失败");
            }

            // ========== 步骤4: 批量插入订单明细 ==========
            for (OrderItem item : orderItems) {
                item.setOrder_no(orderNo);  // 设置订单号关联
            }
            int itemResult = orderItemMapper.batchInsert(orderItems);
            if (itemResult <= 0) {
                throw new RuntimeException("订单明细创建失败");
            }

            // ========== 步骤5: 原子扣减库存（防止超卖） ==========
            for (CartItem cartItem : cartItems) {
                int rows = productsMapper.decreaseStockAtomic(cartItem.getSku_id(), cartItem.getQuantity());
                if (rows <= 0) {
                    throw new RuntimeException("库存扣减失败 - SKU: " + cartItem.getSku_id());
                }
            }

            // ========== 步骤6: 清空购物车 ==========
            if (cartItemIds == null || cartItemIds.isEmpty()) {
                // 清空全部购物车
                cartItemMapper.deleteByUserId(user_id);
            } else {
                // 删除指定的购物车项
                cartItemMapper.batchDelete(cartItemIds);
            }

            logger.info("购物车转订单成功 - 订单号: {}, 用户ID: {}, 商品数量: {}",
                orderNo, user_id, orderItems.size());
            return orderNo;

        } catch (Exception e) {
            logger.error("购物车转订单异常 - 用户ID: {}, 错误: {}", user_id, e.getMessage());
            // 抛出运行时异常，触发事务回滚
            throw new RuntimeException("购物车转订单失败: " + e.getMessage(), e);
        }
    }

    /**
     * 更新订单信息
     *
     * @param order 订单对象（必须包含order_no）
     * @return 更新成功返回true，失败返回false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOrder(Order order) {
        try {
            if (order.getOrder_no() == null || order.getOrder_no().isEmpty()) {
                logger.warn("更新订单失败：订单号为空");
                return false;
            }
            Order existingOrder = orderMapper.findByOrderNo(order.getOrder_no());
            if (existingOrder == null) {
                logger.warn("更新订单失败：订单不存在 - 订单号: {}", order.getOrder_no());
                return false;
            }
            int result = orderMapper.update(order);
            if (result > 0) {
                logger.info("订单更新成功 - 订单号: {}", order.getOrder_no());
                return true;
            } else {
                logger.warn("订单更新失败 - 订单号: {}", order.getOrder_no());
                return false;
            }
        } catch (Exception e) {
            logger.error("订单更新异常 - 订单号: {}, 错误: {}", order.getOrder_no(), e.getMessage());
            return false;
        }
    }

    /**
     * 根据订单号删除订单（级联删除订单明细）
     *
     * @param order_no 订单编号
     * @return 删除成功返回true，失败返回false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByOrderNo(String order_no) {
        try {
            if (order_no == null || order_no.isEmpty()) {
                logger.warn("删除订单失败：订单号为空");
                return false;
            }
            Order existingOrder = orderMapper.findByOrderNo(order_no);
            if (existingOrder == null) {
                logger.warn("删除订单失败：订单不存在 - 订单号: {}", order_no);
                return false;
            }
            orderItemMapper.deleteByOrderNo(order_no);
            int result = orderMapper.deleteByOrderNo(order_no);
            if (result > 0) {
                logger.info("订单删除成功 - 订单号: {}", order_no);
                return true;
            } else {
                logger.warn("订单删除失败 - 订单号: {}", order_no);
                return false;
            }
        } catch (Exception e) {
            logger.error("订单删除异常 - 订单号: {}, 错误: {}", order_no, e.getMessage());
            return false;
        }
    }

    /**
     * 更新订单状态
     *
     * @param order_no 订单编号
     * @param status 新状态
     * @return 更新成功返回true，失败返回false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(String order_no, String status) {
        try {
            Order order = orderMapper.findByOrderNo(order_no);
            if (order == null) {
                logger.warn("更新订单状态失败：订单不存在 - 订单号: {}", order_no);
                return false;
            }
            int result = orderMapper.updateStatus(order_no, status);
            if (result > 0) {
                logger.info("订单状态更新成功 - 订单号: {}, 新状态: {}", order_no, status);
                return true;
            } else {
                logger.warn("订单状态更新失败 - 订单号: {}", order_no);
                return false;
            }
        } catch (Exception e) {
            logger.error("订单状态更新异常 - 订单号: {}, 错误: {}", order_no, e.getMessage());
            return false;
        }
    }

    /**
     * 支付订单
     *
     * <p>验证订单状态为待支付，校验支付金额不小于订单总额，然后更新支付信息。
     *
     * @param order_no 订单编号
     * @param payment_method 支付方式
     * @param pay_amount 支付金额
     * @return 支付成功返回true，失败返回false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean payOrder(String order_no, String payment_method, Double pay_amount) {
        try {
            Order order = orderMapper.findByOrderNo(order_no);
            if (order == null) {
                logger.warn("支付订单失败：订单不存在 - 订单号: {}", order_no);
                return false;
            }
            if (!STATUS_PENDING.equals(order.getStatus())) {
                logger.warn("支付订单失败：订单状态不是待支付 - 订单号: {}, 当前状态: {}", order_no, order.getStatus());
                return false;
            }
            if (pay_amount == null || pay_amount < order.getTotal_amount()) {
                logger.warn("支付订单失败：支付金额不足 - 订单号: {}, 应付: {}, 实付: {}",
                        order_no, order.getTotal_amount(), pay_amount);
                return false;
            }
            int result = orderMapper.updatePayment(order_no, payment_method, pay_amount);
            if (result > 0) {
                logger.info("订单支付成功 - 订单号: {}, 支付方式: {}, 支付金额: {}",
                    order_no, payment_method, pay_amount);
                return true;
            } else {
                logger.warn("订单支付失败 - 订单号: {}", order_no);
                return false;
            }
        } catch (Exception e) {
            logger.error("订单支付异常 - 订单号: {}, 错误: {}", order_no, e.getMessage());
            return false;
        }
    }

    /**
     * 订单发货
     *
     * <p>验证订单状态为已支付，然后更新发货时间和状态。
     *
     * @param order_no 订单编号
     * @return 发货成功返回true，失败返回false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deliverOrder(String order_no) {
        try {
            Order order = orderMapper.findByOrderNo(order_no);
            if (order == null) {
                logger.warn("发货失败：订单不存在 - 订单号: {}", order_no);
                return false;
            }
            if (!STATUS_PAID.equals(order.getStatus())) {
                logger.warn("发货失败：订单状态不是已支付 - 订单号: {}, 当前状态: {}", order_no, order.getStatus());
                return false;
            }
            int result = orderMapper.updateDeliveryTime(order_no, new Date());
            if (result > 0) {
                logger.info("订单发货成功 - 订单号: {}", order_no);
                return true;
            } else {
                logger.warn("订单发货失败 - 订单号: {}", order_no);
                return false;
            }
        } catch (Exception e) {
            logger.error("订单发货异常 - 订单号: {}, 错误: {}", order_no, e.getMessage());
            return false;
        }
    }

    /**
     * 完成订单
     *
     * <p>验证订单状态为已发货，然后更新状态为已完成。
     *
     * @param order_no 订单编号
     * @return 完成成功返回true，失败返回false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeOrder(String order_no) {
        try {
            Order order = orderMapper.findByOrderNo(order_no);
            if (order == null) {
                logger.warn("完成订单失败：订单不存在 - 订单号: {}", order_no);
                return false;
            }
            if (!STATUS_DELIVERED.equals(order.getStatus())) {
                logger.warn("完成订单失败：订单状态不是已发货 - 订单号: {}, 当前状态: {}", order_no, order.getStatus());
                return false;
            }
            int result = orderMapper.updateStatus(order_no, STATUS_COMPLETED);
            if (result > 0) {
                logger.info("订单完成成功 - 订单号: {}", order_no);
                return true;
            } else {
                logger.warn("订单完成失败 - 订单号: {}", order_no);
                return false;
            }
        } catch (Exception e) {
            logger.error("订单完成异常 - 订单号: {}, 错误: {}", order_no, e.getMessage());
            return false;
        }
    }

    /**
     * 取消订单
     * 
     * <p>验证订单状态允许取消（已完成和已取消的订单不能取消）。
     * 如果订单已支付或已发货，会遍历订单明细并恢复商品库存。
     * 最后将订单状态更新为已取消。
     * 
     * @param order_no 订单编号
     * @return 取消成功返回true，失败返回false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(String order_no) {
        try {
            Order order = orderMapper.findByOrderNo(order_no);
            if (order == null) {
                logger.warn("取消订单失败：订单不存在 - 订单号: {}", order_no);
                return false;
            }
            String status = order.getStatus();
            // 校验订单状态是否允许取消
            if (STATUS_COMPLETED.equals(status) || STATUS_CANCELLED.equals(status)) {
                logger.warn("取消订单失败：订单状态不允许取消 - 订单号: {}, 当前状态: {}", order_no, status);
                return false;
            }

            // 如果订单已支付或已发货，恢复库存
            if (STATUS_PAID.equals(status) || STATUS_DELIVERED.equals(status)) {
                List<OrderItem> items = orderItemMapper.findByOrderNo(order_no);
                if (items != null) {
                    for (OrderItem item : items) {
                        Products product = productsMapper.findBySpid(item.getSku_id());
                        if (product != null && product.getStock() != null) {
                            int restoredStock = product.getStock() + item.getQuantity();
                            productsMapper.updateStock(item.getSku_id(), restoredStock);
                            logger.info("取消订单恢复库存 - SKU: {}, 数量: {}, 恢复后库存: {}",
                                    item.getSku_id(), item.getQuantity(), restoredStock);
                        }
                    }
                }
            }

            // 更新订单状态为已取消
            int result = orderMapper.updateStatus(order_no, STATUS_CANCELLED);
            if (result > 0) {
                logger.info("订单取消成功 - 订单号: {}", order_no);
                return true;
            } else {
                logger.warn("订单取消失败 - 订单号: {}", order_no);
                return false;
            }
        } catch (Exception e) {
            logger.error("订单取消异常 - 订单号: {}, 错误: {}", order_no, e.getMessage());
            return false;
        }
    }

    /**
     * 生成订单编号
     * 
     * <p>格式：YYYYMMDDHHmmss + 毫秒后2位 + 4位序号
     * 例如：20260525143022981234
     * 
     * <p>组成说明：
     * <ul>
     *   <li>YYYYMMDDHHmmss (14位): 年月日时分秒</li>
     *   <li>毫秒后2位 (2位): 00-99</li>
     *   <li>4位序号 (4位): 0000-9999，循环使用</li>
     * </ul>
     * 
     * <p>总长度20位，确保同一秒内最多可生成100万唯一订单号（100毫秒 * 10000序号）。
     * 使用 AtomicInteger 保证并发安全。
     * 
     * @return 订单编号字符串
     */
    private String generateOrderNo() {
        Date now = new Date();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMddHHmmss");
        StringBuilder sb = new StringBuilder();
        sb.append(sdf.format(now));
        // 毫秒后2位（0-99）
        long ms = System.currentTimeMillis() % 100;
        sb.append(String.format("%02d", ms));
        // 4位递增序号（0000-9999），循环使用
        int seq = seqCounter.incrementAndGet() % 10000;
        sb.append(String.format("%04d", seq));
        return sb.toString();
    }
}