package com.weep.mapper;

import com.weep.entity.Products;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductsMapper {

    Products findBySpid(@Param("spid") String spid);

    Products findByPName(@Param("p_name") String p_name);

    List<Products> findAll();

    List<Products> findByCategoryId(@Param("category_id") String category_id);

    List<Products> findByStatus(@Param("status") String status);

    int insert(Products product);

    int update(Products product);

    int deleteBySpid(@Param("spid") String spid);

    int updateStatus(@Param("spid") String spid, @Param("status") String status);

    int updateStock(@Param("spid") String spid, @Param("stock") int stock);

    /**
     * 原子性库存扣减
     * 直接在 SQL 中执行 stock = stock - quantity，只在 stock >= quantity 时生效
     * @return 影响的行数，0 表示库存不足或商品不存在
     */
    int decreaseStockAtomic(@Param("spid") String spid, @Param("quantity") int quantity);

    /** 搜索商品（关键词+分类+状态，带分页和排序） */
    List<Products> search(@Param("keyword") String keyword,
                          @Param("category_id") String category_id,
                          @Param("status") String status,
                          @Param("sort_by") String sort_by,
                          @Param("sort_order") String sort_order,
                          @Param("offset") int offset,
                          @Param("limit") int limit);

    /** 搜索商品总数 */
    long countSearch(@Param("keyword") String keyword,
                     @Param("category_id") String category_id,
                     @Param("status") String status);
}
