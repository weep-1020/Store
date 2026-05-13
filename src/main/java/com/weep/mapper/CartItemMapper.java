package com.weep.mapper;

import com.weep.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 购物车项数据访问接口
 */
@Mapper
public interface CartItemMapper {

    /**
     * 根据ID查询购物车项
     * @param id 主键ID
     * @return 购物车项对象
     */
    CartItem findById(@Param("id") Long id);

    /**
     * 根据ID列表批量查询购物车项
     * @param ids ID列表
     * @return 购物车项列表
     */
    List<CartItem> findByIds(@Param("ids") List<Long> ids);

    /**
     * 根据用户ID查询购物车列表
     * @param user_id 用户ID
     * @return 购物车项列表
     */
    List<CartItem> findByUserId(@Param("user_id") Long user_id);

    /**
     * 根据用户ID和SKU ID查询购物车项
     * @param user_id 用户ID
     * @param sku_id SKU ID
     * @return 购物车项对象
     */
    CartItem findByUserIdAndSkuId(@Param("user_id") Long user_id, @Param("sku_id") String sku_id);

    /**
     * 插入购物车项
     * @param cartItem 购物车项对象
     * @return 影响行数
     */
    int insert(CartItem cartItem);

    /**
     * 更新购物车项数量
     * @param id 主键ID
     * @param quantity 新数量
     * @return 影响行数
     */
    int updateQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 删除购物车项
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据用户ID删除购物车项
     * @param user_id 用户ID
     * @return 影响行数
     */
    int deleteByUserId(@Param("user_id") Long user_id);

    /**
     * 根据用户ID和SKU ID删除购物车项
     * @param user_id 用户ID
     * @param sku_id SKU ID
     * @return 影响行数
     */
    int deleteByUserIdAndSkuId(@Param("user_id") Long user_id, @Param("sku_id") String sku_id);

    /**
     * 批量删除购物车项
     * @param ids ID列表
     * @return 影响行数
     */
    int batchDelete(@Param("ids") List<Long> ids);
}