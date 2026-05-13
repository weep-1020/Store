package com.weep.service.impl;

import com.weep.entity.OrderItem;
import com.weep.mapper.OrderItemMapper;
import com.weep.service.OrderItemService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单明细服务实现类
 *
 * <p>实现订单明细的管理功能，包括订单明细的增删改查和批量操作。
 * 使用 @Transactional 注解保证事务一致性。
 */
@Service
public class OrderItemServiceImpl implements OrderItemService {

    private static final Logger logger = LoggerFactory.getLogger(OrderItemServiceImpl.class);

    @Resource
    private OrderItemMapper orderItemMapper;

    /**
     * 根据ID查询订单明细
     *
     * @param id 主键ID
     * @return 订单明细对象，不存在则返回null
     */
    @Override
    public OrderItem findById(Long id) {
        try {
            return orderItemMapper.findById(id);
        } catch (Exception e) {
            logger.error("查询订单明细失败 - ID: {}, 错误: {}", id, e.getMessage());
            return null;
        }
    }

    /**
     * 根据订单编号查询订单明细列表
     *
     * @param order_no 订单编号
     * @return 订单明细列表，无数据则返回空列表
     */
    @Override
    public List<OrderItem> findByOrderNo(String order_no) {
        try {
            return orderItemMapper.findByOrderNo(order_no);
        } catch (Exception e) {
            logger.error("查询订单明细失败 - 订单号: {}, 错误: {}", order_no, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 查询所有订单明细
     *
     * @return 订单明细列表，无数据则返回空列表
     */
    @Override
    public List<OrderItem> findAll() {
        try {
            return orderItemMapper.findAll();
        } catch (Exception e) {
            logger.error("查询所有订单明细失败 - 错误: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 添加订单明细
     *
     * @param orderItem 订单明细对象
     * @return 添加成功返回true，失败返回false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addOrderItem(OrderItem orderItem) {
        try {
            if (orderItem.getOrder_no() == null || orderItem.getOrder_no().isEmpty()) {
                logger.warn("添加订单明细失败：订单号为空");
                return false;
            }

            if (orderItem.getSku_id() == null || orderItem.getSku_id().isEmpty()) {
                logger.warn("添加订单明细失败：商品SKU ID为空");
                return false;
            }

            if (orderItem.getQuantity() == null || orderItem.getQuantity() <= 0) {
                logger.warn("添加订单明细失败：数量无效");
                return false;
            }

            int result = orderItemMapper.insert(orderItem);
            if (result > 0) {
                logger.info("订单明细添加成功 - ID: {}, 订单号: {}", orderItem.getId(), orderItem.getOrder_no());
                return true;
            } else {
                logger.warn("订单明细添加失败 - 订单号: {}", orderItem.getOrder_no());
                return false;
            }
        } catch (Exception e) {
            logger.error("订单明细添加异常 - 订单号: {}, 错误: {}", orderItem.getOrder_no(), e.getMessage());
            return false;
        }
    }

    /**
     * 更新订单明细
     *
     * @param orderItem 订单明细对象（必须包含id）
     * @return 更新成功返回true，失败返回false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOrderItem(OrderItem orderItem) {
        try {
            if (orderItem.getId() == null) {
                logger.warn("更新订单明细失败：ID为空");
                return false;
            }

            OrderItem existingItem = orderItemMapper.findById(orderItem.getId());
            if (existingItem == null) {
                logger.warn("更新订单明细失败：订单明细不存在 - ID: {}", orderItem.getId());
                return false;
            }

            int result = orderItemMapper.update(orderItem);
            if (result > 0) {
                logger.info("订单明细更新成功 - ID: {}", orderItem.getId());
                return true;
            } else {
                logger.warn("订单明细更新失败 - ID: {}", orderItem.getId());
                return false;
            }
        } catch (Exception e) {
            logger.error("订单明细更新异常 - ID: {}, 错误: {}", orderItem.getId(), e.getMessage());
            return false;
        }
    }

    /**
     * 根据ID删除订单明细
     *
     * @param id 主键ID
     * @return 删除成功返回true，失败返回false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteById(Long id) {
        try {
            if (id == null) {
                logger.warn("删除订单明细失败：ID为空");
                return false;
            }

            OrderItem existingItem = orderItemMapper.findById(id);
            if (existingItem == null) {
                logger.warn("删除订单明细失败：订单明细不存在 - ID: {}", id);
                return false;
            }

            int result = orderItemMapper.deleteById(id);
            if (result > 0) {
                logger.info("订单明细删除成功 - ID: {}", id);
                return true;
            } else {
                logger.warn("订单明细删除失败 - ID: {}", id);
                return false;
            }
        } catch (Exception e) {
            logger.error("订单明细删除异常 - ID: {}, 错误: {}", id, e.getMessage());
            return false;
        }
    }

    /**
     * 根据订单编号删除订单明细
     *
     * @param order_no 订单编号
     * @return 删除成功返回true，失败返回false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByOrderNo(String order_no) {
        try {
            if (order_no == null || order_no.isEmpty()) {
                logger.warn("删除订单明细失败：订单号为空");
                return false;
            }

            int result = orderItemMapper.deleteByOrderNo(order_no);
            if (result > 0) {
                logger.info("订单明细批量删除成功 - 订单号: {}", order_no);
                return true;
            } else {
                logger.warn("订单明细批量删除失败 - 订单号: {}", order_no);
                return false;
            }
        } catch (Exception e) {
            logger.error("订单明细批量删除异常 - 订单号: {}, 错误: {}", order_no, e.getMessage());
            return false;
        }
    }

    /**
     * 批量添加订单明细
     *
     * @param orderItems 订单明细列表
     * @return 添加成功返回true，失败返回false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchAddOrderItems(List<OrderItem> orderItems) {
        try {
            if (orderItems == null || orderItems.isEmpty()) {
                logger.warn("批量添加订单明细失败：列表为空");
                return false;
            }

            int result = orderItemMapper.batchInsert(orderItems);
            if (result > 0) {
                logger.info("订单明细批量添加成功 - 数量: {}", result);
                return true;
            } else {
                logger.warn("订单明细批量添加失败");
                return false;
            }
        } catch (Exception e) {
            logger.error("订单明细批量添加异常 - 错误: {}", e.getMessage());
            return false;
        }
    }
}