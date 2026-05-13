package com.weep.service;

import com.weep.entity.CartItem;

import java.util.List;

/**
 * 购物车服务接口
 *
 * <p>提供用户购物车的完整管理功能，包括商品添加、数量修改、删除、清空等操作。
 * 支持同一商品的数量合并，以及批量操作。
 */
public interface CartService {

    /**
     * 查询指定用户的购物车列表
     *
     * @param userId 用户ID
     * @return 用户的购物车项列表，无数据则返回空列表
     */
    List<CartItem> findByUserId(Long userId);

    /**
     * 添加商品到购物车
     *
     * <p>如果该商品已存在于购物车中，则增加数量；否则创建新的购物车项。
     *
     * @param userId 用户ID
     * @param skuId 商品SKU ID
     * @param quantity 数量（必须大于0）
     * @return 添加成功的购物车项
     */
    CartItem addToCart(Long userId, String skuId, Integer quantity);

    /**
     * 更新购物车项数量
     *
     * @param userId 用户ID
     * @param cartItemId 购物车项ID
     * @param quantity 新数量（大于0）
     * @return 更新成功返回true，失败返回false
     */
    boolean updateQuantity(Long userId, Long cartItemId, Integer quantity);

    /**
     * 删除指定购物车项
     *
     * @param userId 用户ID
     * @param cartItemId 购物车项ID
     * @return 删除成功返回true，失败返回false
     */
    boolean removeById(Long userId, Long cartItemId);

    /**
     * 清空购物车
     *
     * @param userId 用户ID
     * @return 清空成功返回true，失败返回false
     */
    boolean clearCart(Long userId);

    /**
     * 批量删除购物车项
     *
     * @param userId 用户ID
     * @param ids 要删除的购物车项ID列表
     * @return 删除成功返回true，失败返回false
     */
    boolean batchRemove(Long userId, List<Long> ids);
}