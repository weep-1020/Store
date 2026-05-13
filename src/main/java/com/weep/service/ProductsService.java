package com.weep.service;

import com.weep.entity.Products;
import com.weep.response.PageResult;

import java.util.List;

/**
 * 商品服务接口
 *
 * <p>提供商品管理的核心功能，包括商品的增删改查、库存管理、上下架等操作。
 */
public interface ProductsService {

    /**
     * 根据商品ID查询商品
     *
     * @param spid 商品ID
     * @return 商品对象，不存在则返回null
     */
    Products findBySpid(String spid);

    /**
     * 根据商品名称查询商品
     *
     * @param p_name 商品名称
     * @return 商品对象，不存在则返回null
     */
    Products findByPName(String p_name);

    /**
     * 查询所有商品
     *
     * @return 商品列表
     */
    List<Products> findAll();

    /**
     * 根据分类ID查询商品
     *
     * @param category_id 分类ID
     * @return 该分类下的商品列表
     */
    List<Products> findByCategoryId(String category_id);

    /**
     * 根据状态查询商品
     *
     * @param status 商品状态（0-下架，1-上架）
     * @return 对应状态的商品列表
     */
    List<Products> findByStatus(String status);

    /**
     * 添加商品
     *
     * @param product 商品对象
     * @return 添加成功返回true，失败返回false
     */
    boolean addProduct(Products product);

    /**
     * 更新商品信息
     *
     * @param product 商品对象（必须包含spid）
     * @return 更新成功返回true，失败返回false
     */
    boolean updateProduct(Products product);

    /**
     * 根据商品ID删除商品
     *
     * @param spid 商品ID
     * @return 删除成功返回true，失败返回false
     */
    boolean deleteBySpid(String spid);

    /**
     * 更新商品状态（上架/下架）
     *
     * @param spid 商品ID
     * @param status 商品状态
     * @return 更新成功返回true，失败返回false
     */
    boolean updateStatus(String spid, String status);

    /**
     * 更新商品库存
     *
     * @param spid 商品ID
     * @param stock 新库存数量
     * @return 更新成功返回true，失败返回false
     */
    boolean updateStock(String spid, int stock);

    /**
     * 减少商品库存（原子操作）
     *
     * <p>用于订单创建时扣减库存，保证原子性。
     *
     * @param spid 商品ID
     * @param quantity 要减少的数量
     * @return 扣减成功返回true，库存不足返回false
     */
    boolean decreaseStock(String spid, int quantity);

    /**
     * 搜索商品（关键词+分类+状态，带分页和排序）
     *
     * @param keyword 搜索关键词（支持名称模糊匹配）
     * @param category_id 分类ID（可选）
     * @param status 商品状态（可选）
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param sortBy 排序字段
     * @param sortOrder 排序方向（asc/desc）
     * @return 分页结果
     */
    PageResult<Products> search(String keyword, String category_id, String status,
                                int page, int size, String sortBy, String sortOrder);
}