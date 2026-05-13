package com.weep.controller;

import com.weep.dto.BatchAddOrderItemsRequest;
import com.weep.entity.OrderItem;
import com.weep.response.Result;
import com.weep.service.OrderItemService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单明细控制器
 *
 * <p>负责处理订单明细（OrderItem）相关的 REST API 请求。
 * 订单明细是指订单中的单个商品项，包含商品ID、数量、单价等信息。
 *
 * <p>主要功能：
 * <ul>
 *   <li>订单明细查询（按ID、按订单号、查询全部）</li>
 *   <li>订单明细增删改</li>
 *   <li>批量添加订单明细</li>
 * </ul>
 *
 * <p>权限要求：
 * <ul>
 *   <li>查询操作：SUPER_ADMIN 或 MERCHANT</li>
 *   <li>修改操作：SUPER_ADMIN 或 MERCHANT</li>
 *   <li>批量添加：SUPER_ADMIN 或 MERCHANT</li>
 * </ul>
 *
 * <p>注意：普通用户不直接操作订单明细，而是通过订单接口间接访问。
 *
 * @author Weep
 * @version 1.0
 */
@RestController
@RequestMapping("/api/order-items")
public class OrderItemController {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(OrderItemController.class);

    /**
     * 订单明细服务接口
     */
    @Autowired
    private OrderItemService orderItemService;

    /**
     * 根据ID查询订单明细
     *
     * <p>查询指定ID的订单明细详情，包含商品信息、数量、单价等。
     *
     * @param id 订单明细ID
     * @return 订单明细对象
     *         <ul>
     *           <li>200: 查询成功</li>
     *           <li>404: 订单明细不存在</li>
     *         </ul>
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT')")
    public Result<OrderItem> getOrderItemById(@PathVariable Long id) {
        logger.debug("查询订单明细 - ID: {}", id);

        OrderItem orderItem = orderItemService.findById(id);
        if (orderItem != null) {
            return Result.success(orderItem);
        } else {
            logger.warn("订单明细不存在 - ID: {}", id);
            return Result.fail("订单明细不存在");
        }
    }

    /**
     * 根据订单编号查询订单明细列表
     *
     * <p>查询指定订单的所有商品明细，用于展示订单详情页面。
     *
     * @param orderNo 订单编号
     * @return 订单明细列表
     */
    @GetMapping("/order/{orderNo}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT')")
    public Result<List<OrderItem>> getOrderItemsByOrderNo(@PathVariable String orderNo) {
        logger.debug("查询订单明细列表 - 订单号: {}", orderNo);
        return Result.success(orderItemService.findByOrderNo(orderNo));
    }

    /**
     * 查询所有订单明细（管理员专用）
     *
     * <p>仅 SUPER_ADMIN 角色可访问，返回系统中所有订单明细。
     *
     * @return 所有订单明细列表
     */
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<List<OrderItem>> getAllOrderItems() {
        logger.debug("查询所有订单明细");
        return Result.success(orderItemService.findAll());
    }

    /**
     * 添加订单明细
     *
     * <p>手动添加单个订单明细，通常用于后台管理系统。
     *
     * @param orderItem 订单明细对象，包含：
     *                  <ul>
     *                    <li>order_no: 订单号</li>
     *                    <li>product_id: 商品ID</li>
     *                    <li>sku_id: SKU ID</li>
     *                    <li>quantity: 数量</li>
     *                    <li>price: 单价</li>
     *                  </ul>
     * @return 添加结果
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT')")
    public Result<Void> addOrderItem(@RequestBody OrderItem orderItem) {
        logger.debug("添加订单明细 - SKU: {}", orderItem.getSku_id());

        boolean success = orderItemService.addOrderItem(orderItem);
        if (success) {
            logger.info("订单明细添加成功 - SKU: {}", orderItem.getSku_id());
            return Result.success();
        } else {
            logger.warn("订单明细添加失败");
            return Result.fail("添加失败");
        }
    }

    /**
     * 更新订单明细
     *
     * <p>更新订单明细的数量、价格等信息。
     *
     * @param id 订单明细ID
     * @param orderItem 更新的订单明细数据
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT')")
    public Result<Void> updateOrderItem(@PathVariable Long id, @RequestBody OrderItem orderItem) {
        logger.debug("更新订单明细 - ID: {}", id);

        orderItem.setId(id);
        boolean success = orderItemService.updateOrderItem(orderItem);
        if (success) {
            logger.info("订单明细更新成功 - ID: {}", id);
            return Result.success();
        } else {
            logger.warn("订单明细更新失败 - ID: {}", id);
            return Result.fail("更新失败");
        }
    }

    /**
     * 删除订单明细
     *
     * <p>删除指定的订单明细记录。
     *
     * @param id 订单明细ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT')")
    public Result<Void> deleteOrderItem(@PathVariable Long id) {
        logger.debug("删除订单明细 - ID: {}", id);

        boolean success = orderItemService.deleteById(id);
        if (success) {
            logger.info("订单明细删除成功 - ID: {}", id);
            return Result.success();
        } else {
            logger.warn("订单明细删除失败 - ID: {}", id);
            return Result.fail("删除失败");
        }
    }

    /**
     * 批量添加订单明细
     *
     * <p>一次性为某个订单添加多个商品明细，提高批量操作效率。
     * 通常用于购物车转订单时批量迁移购物项。
     *
     * @param request 批量添加请求，包含：
     *                <ul>
     *                  <li>order_no: 订单号</li>
     *                  <li>items: 订单明细列表</li>
     *                </ul>
     * @return 批量添加结果
     */
    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT')")
    public Result<Void> batchAddOrderItems(@Valid @RequestBody BatchAddOrderItemsRequest request) {
        logger.debug("批量添加订单明细 - 订单号: {}, 商品数量: {}",
                request.getOrder_no(), request.getItems().size());

        // 为每个订单项设置订单号
        for (OrderItem item : request.getItems()) {
            item.setOrder_no(request.getOrder_no());
        }

        boolean success = orderItemService.batchAddOrderItems(request.getItems());
        if (success) {
            logger.info("批量添加订单明细成功 - 订单号: {}, 商品数量: {}",
                    request.getOrder_no(), request.getItems().size());
            return Result.success();
        } else {
            logger.warn("批量添加订单明细失败 - 订单号: {}", request.getOrder_no());
            return Result.fail("批量添加失败");
        }
    }
}
