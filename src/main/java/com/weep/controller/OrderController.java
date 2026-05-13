package com.weep.controller;

import com.weep.dto.CreateOrderFromCartRequest;
import com.weep.dto.PayOrderRequest;
import com.weep.entity.Order;
import com.weep.entity.OrderItem;
import com.weep.entity.User;
import com.weep.response.Result;
import com.weep.service.OrderItemService;
import com.weep.service.OrderService;
import com.weep.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单控制器
 *
 * <p>负责处理订单相关的 REST API 请求，包括订单查询、创建、支付、发货、完成、取消等全生命周期管理。
 * 支持用户个人订单管理和商家/管理员的订单操作。
 *
 * <p>主要功能：
 * <ul>
 *   <li>订单查询（个人订单、订单详情、状态筛选）</li>
 *   <li>购物车转订单（核心业务逻辑）</li>
 *   <li>订单支付、发货、完成、取消</li>
 *   <li>订单明细查询</li>
 *   <li>权限控制（用户只能访问自己的订单，商家可管理相关订单）</li>
 * </ul>
 *
 * <p>权限说明：
 * <ul>
 *   <li>USER: 只能查询和操作自己的订单</li>
 *   <li>MERCHANT: 可以查询和管理商家相关订单</li>
 *   <li>SUPER_ADMIN: 可以访问所有订单</li>
 * </ul>
 *
 * @author Weep
 * @version 1.0
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    /**
     * 订单服务接口
     */
    @Autowired
    private OrderService orderService;

    /**
     * 订单明细服务接口
     */
    @Autowired
    private OrderItemService orderItemService;

    /**
     * 用户服务接口
     */
    @Autowired
    private UserService userService;

    /**
     * 查询当前用户的订单列表
     *
     * <p>返回当前登录用户的所有订单，按创建时间倒序排列。
     *
     * @param authentication Spring Security 认证对象，用于获取当前用户信息
     * @return 订单列表
     *         <ul>
     *           <li>200: 查询成功，返回订单列表</li>
     *           <li>401: 未认证</li>
     *         </ul>
     */
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT', 'USER')")
    public Result<List<Order>> getMyOrders(Authentication authentication) {
        Long userId = getUserId(authentication);
        if (userId == null) return Result.error(401, "未认证");
        return Result.success(orderService.findByUserId(userId));
    }

    /**
     * 从购物车创建订单（用户端）
     *
     * <p>将选中的购物车项转换为正式订单，这是电商系统的核心业务流程。
     * 后端会执行库存校验、原子扣减、订单号生成、购物项迁移等操作。
     *
     * <p>处理流程：
     * <ol>
     *   <li>验证用户身份</li>
     *   <li>提取购物车项ID列表</li>
     *   <li>封装收货信息</li>
     *   <li>调用服务层创建订单（事务处理）</li>
     *   <li>返回订单号</li>
     * </ol>
     *
     * @param request 创建订单请求体，包含：
     *                <ul>
     *                  <li>cartItemIds: 选中的购物车项ID列表（可选，为空则转换全部）</li>
     *                  <li>receiver_name: 收货人姓名</li>
     *                  <li>receiver_phone: 收货人电话</li>
     *                  <li>receiver_address: 收货地址</li>
     *                  <li>remark: 订单备注（可选）</li>
     *                </ul>
     * @param authentication Spring Security 认证对象
     * @return 创建结果
     *         <ul>
     *           <li>200: 创建成功，data包含order_no</li>
     *           <li>401: 未认证</li>
     *           <li>400: 参数错误（库存不足、商品下架等）</li>
     *           <li>500: 系统错误</li>
     *         </ul>
     */
    @PostMapping("/my/from-cart")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT', 'USER')")
    public Result<Map<String, String>> createMyOrderFromCart(
            @Valid @RequestBody CreateOrderFromCartRequest request,
            Authentication authentication) {

        Long userId = getUserId(authentication);
        if (userId == null) return Result.error(401, "未认证");

        logger.info("用户购物车转订单 - 用户ID: {}", userId);

        // 提取购物车项ID列表（过滤无效ID）
        List<Long> cartItemIds = null;
        if (request.getCartItemIds() != null && !request.getCartItemIds().isEmpty()) {
            cartItemIds = request.getCartItemIds().stream()
                    .filter(id -> id != null && id > 0)
                    .collect(Collectors.toList());
        }

        // 封装收货信息
        Map<String, String> receiverInfo = Map.of(
                "receiver_name", request.getReceiver_name(),
                "receiver_phone", request.getReceiver_phone(),
                "receiver_address", request.getReceiver_address(),
                "remark", request.getRemark() != null ? request.getRemark() : ""
        );

        try {
            // 调用服务层创建订单（事务处理）
            String orderNo = orderService.createOrderFromCart(userId, cartItemIds, receiverInfo);
            logger.info("购物车转订单成功 - 用户: {}, 订单号: {}", userId, orderNo);
            return Result.success(Map.of("order_no", orderNo));
        } catch (RuntimeException e) {
            logger.error("购物车转订单失败 - 用户: {}, 错误: {}", userId, e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 根据订单号查询订单详情
     *
     * <p>需要验证订单所有权，用户只能查询自己的订单，管理员和商家可查询所有订单。
     *
     * @param orderNo 订单号
     * @param authentication Spring Security 认证对象
     * @return 订单详情
     *         <ul>
     *           <li>200: 查询成功</li>
     *           <li>401: 未认证</li>
     *           <li>403: 无权访问此订单</li>
     *           <li>404: 订单不存在</li>
     *         </ul>
     */
    @GetMapping("/no/{orderNo}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT', 'USER')")
    @SuppressWarnings("unchecked")
    public Result<Order> getOrderByOrderNo(@PathVariable String orderNo, Authentication authentication) {
        Result<Void> ownership = checkOrderOwnership(orderNo, authentication);
        if (ownership != null) return (Result<Order>) (Object) ownership;
        Order order = orderService.findByOrderNo(orderNo);
        if (order != null) {
            return Result.success(order);
        } else {
            return Result.fail("订单不存在");
        }
    }

    /**
     * 根据用户ID查询订单列表
     *
     * <p>普通用户只能查询自己的订单，管理员可以查询任意用户的订单。
     *
     * @param userId 用户ID
     * @param authentication Spring Security 认证对象
     * @return 订单列表
     *         <ul>
     *           <li>200: 查询成功</li>
     *           <li>401: 未认证</li>
     *           <li>403: 无权访问他人订单</li>
     *         </ul>
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT', 'USER')")
    public Result<List<Order>> getOrdersByUserId(@PathVariable Long userId, Authentication authentication) {
        if (!isAdmin(authentication)) {
            Long currentUserId = getUserId(authentication);
            if (currentUserId == null) return Result.error(401, "未认证");
            if (!currentUserId.equals(userId)) return Result.error(403, "无权访问他人订单");
        }
        return Result.success(orderService.findByUserId(userId));
    }

    /**
     * 根据订单状态查询订单列表
     *
     * <p>查询当前用户的指定状态订单，如待支付、待发货、已完成等。
     *
     * @param status 订单状态（pending/paid/delivered/completed/cancelled）
     * @param authentication Spring Security 认证对象
     * @return 订单列表
     *         <ul>
     *           <li>200: 查询成功</li>
     *           <li>401: 未认证</li>
     *         </ul>
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT', 'USER')")
    public Result<List<Order>> getOrdersByStatus(@PathVariable String status,
                                                 Authentication authentication) {
        Long userId = getUserId(authentication);
        if (userId == null) return Result.error(401, "未认证");
        return Result.success(orderService.findByUserIdAndStatus(userId, status));
    }

    /**
     * 查询所有订单（管理员专用）
     *
     * <p>仅 SUPER_ADMIN 角色可访问，返回系统中所有订单。
     *
     * @return 所有订单列表
     */
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<List<Order>> getAllOrders() {
        return Result.success(orderService.findAll());
    }

    /**
     * 创建订单（通用接口）
     *
     * <p>直接传入订单对象创建，适用于特殊场景。通常使用购物车转订单接口。
     *
     * @param order 订单对象
     * @return 创建结果，data包含order_no
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT', 'USER')")
    public Result<Map<String, String>> createOrder(@RequestBody Order order) {
        boolean success = orderService.createOrder(order);
        if (success) {
            return Result.success(Map.of("order_no", order.getOrder_no()));
        } else {
            return Result.fail("创建失败");
        }
    }

    /**
     * 从购物车创建订单（通用接口）
     *
     * <p>与 /my/from-cart 功能相同，提供不同的路径选择。
     *
     * @param request 创建订单请求体
     * @param authentication Spring Security 认证对象
     * @return 创建结果，data包含order_no
     */
    @PostMapping("/from-cart")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT', 'USER')")
    public Result<Map<String, String>> createOrderFromCart(
            @Valid @RequestBody CreateOrderFromCartRequest request,
            Authentication authentication) {

        Long userId = getUserId(authentication);
        if (userId == null) return Result.error(401, "未认证");

        List<Long> cartItemIds = null;
        if (request.getCartItemIds() != null && !request.getCartItemIds().isEmpty()) {
            cartItemIds = request.getCartItemIds().stream()
                    .filter(id -> id != null && id > 0)
                    .collect(Collectors.toList());
        }

        Map<String, String> receiverInfo = Map.of(
                "receiver_name", request.getReceiver_name(),
                "receiver_phone", request.getReceiver_phone(),
                "receiver_address", request.getReceiver_address(),
                "remark", request.getRemark() != null ? request.getRemark() : ""
        );

        try {
            String orderNo = orderService.createOrderFromCart(userId, cartItemIds, receiverInfo);
            logger.info("购物车转订单成功 - 订单号: {}", orderNo);
            return Result.success(Map.of("order_no", orderNo));
        } catch (RuntimeException e) {
            logger.error("购物车转订单失败 - 用户: {}, 错误: {}", userId, e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 更新订单信息
     *
     * <p>需要验证订单所有权，用户只能更新自己的订单。
     *
     * @param orderNo 订单号
     * @param order 更新的订单数据
     * @param authentication Spring Security 认证对象
     * @return 更新结果
     */
    @PutMapping("/{orderNo}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT', 'USER')")
    public Result<Void> updateOrder(@PathVariable String orderNo, @RequestBody Order order, Authentication authentication) {
        Result<Void> ownership = checkOrderOwnership(orderNo, authentication);
        if (ownership != null) return ownership;
        order.setOrder_no(orderNo);
        boolean success = orderService.updateOrder(order);
        if (success) {
            return Result.success();
        } else {
            return Result.fail("更新失败");
        }
    }

    /**
     * 删除订单（管理员专用）
     *
     * <p>仅 SUPER_ADMIN 角色可访问，物理删除订单记录。
     *
     * @param orderNo 订单号
     * @return 删除结果
     */
    @DeleteMapping("/{orderNo}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> deleteOrder(@PathVariable String orderNo) {
        boolean success = orderService.deleteByOrderNo(orderNo);
        if (success) {
            return Result.success();
        } else {
            return Result.fail("删除失败");
        }
    }

    /**
     * 支付订单
     *
     * <p>用户支付订单，更新订单状态为已支付。需要验证订单所有权。
     *
     * @param orderNo 订单号
     * @param request 支付请求，包含：
     *                <ul>
     *                  <li>payment_method: 支付方式（alipay/wechat/card等）</li>
     *                  <li>pay_amount: 支付金额</li>
     *                </ul>
     * @param authentication Spring Security 认证对象
     * @return 支付结果
     */
    @PostMapping("/{orderNo}/pay")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT', 'USER')")
    public Result<Void> payOrder(@PathVariable String orderNo,
                                 @Valid @RequestBody PayOrderRequest request,
                                 Authentication authentication) {
        Result<Void> ownership = checkOrderOwnership(orderNo, authentication);
        if (ownership != null) return ownership;
        boolean success = orderService.payOrder(orderNo, request.getPayment_method(), request.getPay_amount());
        if (success) {
            return Result.success();
        } else {
            return Result.fail("支付失败");
        }
    }

    /**
     * 发货订单
     *
     * <p>商家或管理员将订单状态更新为已发货。
     *
     * @param orderNo 订单号
     * @return 发货结果
     */
    @PostMapping("/{orderNo}/deliver")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT')")
    public Result<Void> deliverOrder(@PathVariable String orderNo) {
        boolean success = orderService.deliverOrder(orderNo);
        if (success) {
            return Result.success();
        } else {
            return Result.fail("发货失败");
        }
    }

    /**
     * 完成订单
     *
     * <p>用户确认收货，订单状态更新为已完成。需要验证订单所有权。
     *
     * @param orderNo 订单号
     * @param authentication Spring Security 认证对象
     * @return 完成结果
     */
    @PostMapping("/{orderNo}/complete")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT', 'USER')")
    public Result<Void> completeOrder(@PathVariable String orderNo, Authentication authentication) {
        Result<Void> ownership = checkOrderOwnership(orderNo, authentication);
        if (ownership != null) return ownership;
        boolean success = orderService.completeOrder(orderNo);
        if (success) {
            return Result.success();
        } else {
            return Result.fail("完成失败");
        }
    }

    /**
     * 取消订单
     *
     * <p>用户或商家取消订单，释放库存。需要验证订单所有权。
     *
     * @param orderNo 订单号
     * @param authentication Spring Security 认证对象
     * @return 取消结果
     */
    @PostMapping("/{orderNo}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT', 'USER')")
    public Result<Void> cancelOrder(@PathVariable String orderNo, Authentication authentication) {
        Result<Void> ownership = checkOrderOwnership(orderNo, authentication);
        if (ownership != null) return ownership;
        boolean success = orderService.cancelOrder(orderNo);
        if (success) {
            return Result.success();
        } else {
            return Result.fail("取消失败");
        }
    }

    /**
     * 查询订单明细列表
     *
     * <p>获取订单中的所有商品明细，包括商品名称、数量、单价等。
     * 需要验证订单所有权。
     *
     * @param orderNo 订单号
     * @param authentication Spring Security 认证对象
     * @return 订单明细列表
     */
    @GetMapping("/{orderNo}/items")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT', 'USER')")
    @SuppressWarnings("unchecked")
    public Result<List<OrderItem>> getOrderItems(@PathVariable String orderNo, Authentication authentication) {
        Result<Void> ownership = checkOrderOwnership(orderNo, authentication);
        if (ownership != null) return (Result<List<OrderItem>>) (Object) ownership;
        return Result.success(orderItemService.findByOrderNo(orderNo));
    }

    /**
     * 从认证对象中获取用户ID
     *
     * <p>通过手机号查询用户信息，返回用户ID。
     *
     * @param authentication Spring Security 认证对象
     * @return 用户ID，未认证则返回null
     */
    private Long getUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) return null;
        User user = userService.findByPhone(authentication.getName());
        return user != null ? user.getId() : null;
    }

    /**
     * 判断是否为管理员
     *
     * @param authentication Spring Security 认证对象
     * @return true-是管理员，false-不是管理员
     */
    private boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }

    /**
     * 检查订单所有权
     *
     * <p>验证当前用户是否有权访问指定订单。管理员可以访问所有订单，
     * 普通用户只能访问自己的订单。
     *
     * @param orderNo 订单号
     * @param authentication Spring Security 认证对象
     * @return 如果无权访问则返回错误Result，否则返回null
     */
    @SuppressWarnings("unchecked")
    private <T> Result<T> checkOrderOwnership(String orderNo, Authentication authentication) {
        if (isAdmin(authentication)) return null;
        Long userId = getUserId(authentication);
        if (userId == null) return Result.error(401, "未认证");
        Order order = orderService.findByOrderNo(orderNo);
        if (order == null) return Result.fail("订单不存在");
        if (!userId.equals(order.getUser_id())) return Result.error(403, "无权访问此订单");
        return null;
    }
}
