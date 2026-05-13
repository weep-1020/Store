package com.weep.service.impl;

import com.weep.entity.CartItem;
import com.weep.entity.Products;
import com.weep.mapper.CartItemMapper;
import com.weep.mapper.ProductsMapper;
import com.weep.service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 购物车服务实现类
 *
 * <p>实现用户购物车的完整管理功能，包括商品添加、数量修改、删除、清空等操作。
 * 使用 @Transactional 注解保证事务一致性。
 */
@Service
public class CartServiceImpl implements CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);

    @Autowired
    private CartItemMapper cartItemMapper;

    @Autowired
    private ProductsMapper productsMapper;

    /**
     * 查询指定用户的购物车列表
     *
     * @param userId 用户ID
     * @return 用户的购物车项列表，无数据则返回空列表
     */
    @Override
    public List<CartItem> findByUserId(Long userId) {
        try {
            return cartItemMapper.findByUserId(userId);
        } catch (Exception e) {
            logger.error("查询购物车失败 - 用户ID: {}, 错误: {}", userId, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 添加商品到购物车
     *
     * <p>如果该商品已存在于购物车中，则增加数量；否则创建新的购物车项。
     * 同时会检查库存是否充足。
     *
     * @param userId 用户ID
     * @param skuId 商品SKU ID
     * @param quantity 数量（必须大于0）
     * @return 添加成功的购物车项，失败返回null
     */
    @Override
    @Transactional
    public CartItem addToCart(Long userId, String skuId, Integer quantity) {
        Products product = productsMapper.findBySpid(skuId);
        if (product == null) {
            logger.warn("添加购物车失败：商品不存在 - SKU: {}", skuId);
            return null;
        }
        if (quantity == null || quantity <= 0) {
            logger.warn("添加购物车失败：数量无效 - {}", quantity);
            return null;
        }

        // 检查是否已在购物车中
        CartItem existing = cartItemMapper.findByUserIdAndSkuId(userId, skuId);
        if (existing != null) {
            int newQty = existing.getQuantity() + quantity;
            if (product.getStock() != null && newQty > product.getStock()) {
                logger.warn("添加购物车失败：库存不足 - SKU: {}, 库存: {}, 需求: {}", skuId, product.getStock(), newQty);
                return null;
            }
            cartItemMapper.updateQuantity(existing.getId(), newQty);
            existing.setQuantity(newQty);
            logger.info("购物车数量更新 - 用户: {}, SKU: {}, 数量: {}", userId, skuId, newQty);
            return existing;
        }

        if (product.getStock() != null && quantity > product.getStock()) {
            logger.warn("添加购物车失败：库存不足 - SKU: {}, 库存: {}, 需求: {}", skuId, product.getStock(), quantity);
            return null;
        }

        CartItem item = new CartItem();
        item.setUser_id(userId);
        item.setSku_id(skuId);
        item.setProduct_name(product.getP_name());
        item.setProduct_image(product.getPic());
        item.setProduct_price(product.getPrice());
        item.setQuantity(quantity);
        item.setProduct_specs(null); // 产品表无规格字段，保持 null

        cartItemMapper.insert(item);
        logger.info("购物车添加成功 - 用户: {}, SKU: {}, 数量: {}", userId, skuId, quantity);
        return item;
    }

    /**
     * 更新购物车项数量
     *
     * @param userId 用户ID
     * @param cartItemId 购物车项ID
     * @param quantity 新数量（大于0）
     * @return 更新成功返回true，失败返回false
     */
    @Override
    @Transactional
    public boolean updateQuantity(Long userId, Long cartItemId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            return false;
        }
        CartItem item = cartItemMapper.findById(cartItemId);
        if (item == null || !item.getUser_id().equals(userId)) {
            logger.warn("更新购物车数量失败：购物车项不存在或不属于该用户");
            return false;
        }
        Products product = productsMapper.findBySpid(item.getSku_id());
        if (product != null && product.getStock() != null && quantity > product.getStock()) {
            logger.warn("更新购物车数量失败：库存不足 - SKU: {}, 库存: {}, 需求: {}", item.getSku_id(), product.getStock(), quantity);
            return false;
        }
        int result = cartItemMapper.updateQuantity(cartItemId, quantity);
        return result > 0;
    }

    /**
     * 删除指定购物车项
     *
     * @param userId 用户ID
     * @param cartItemId 购物车项ID
     * @return 删除成功返回true，失败返回false
     */
    @Override
    @Transactional
    public boolean removeById(Long userId, Long cartItemId) {
        CartItem item = cartItemMapper.findById(cartItemId);
        if (item == null || !item.getUser_id().equals(userId)) {
            logger.warn("删除购物车项失败：购物车项不存在或不属于该用户");
            return false;
        }
        int result = cartItemMapper.deleteById(cartItemId);
        return result > 0;
    }

    /**
     * 清空购物车
     *
     * @param userId 用户ID
     * @return 清空成功返回true，失败返回false
     */
    @Override
    @Transactional
    public boolean clearCart(Long userId) {
        int result = cartItemMapper.deleteByUserId(userId);
        logger.info("清空购物车 - 用户: {}, 删除: {}", userId, result);
        return result > 0;
    }

    /**
     * 批量删除购物车项
     *
     * @param userId 用户ID
     * @param ids 要删除的购物车项ID列表
     * @return 删除成功返回true，失败返回false
     */
    @Override
    @Transactional
    public boolean batchRemove(Long userId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        List<Long> validIds = new ArrayList<>();
        for (Long id : ids) {
            CartItem item = cartItemMapper.findById(id);
            if (item != null && item.getUser_id().equals(userId)) {
                validIds.add(id);
            }
        }
        if (validIds.isEmpty()) {
            return false;
        }
        int result = cartItemMapper.batchDelete(validIds);
        return result > 0;
    }
}