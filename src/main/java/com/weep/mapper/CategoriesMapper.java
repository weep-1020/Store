package com.weep.mapper;

import com.weep.entity.Categories;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品分类 Mapper 接口
 */
@Mapper
public interface CategoriesMapper {

    /**
     * 根据ID查询分类
     */
    Categories findById(@Param("id") Integer id);

    /**
     * 查询所有分类
     */
    List<Categories> findAll();

    /**
     * 查询父分类（parent_id = 0）
     */
    List<Categories> findParentCategories();

    /**
     * 根据父ID查询子分类
     */
    List<Categories> findByParentId(@Param("parent_id") Integer parent_id);

    /**
     * 添加分类
     */
    int insert(Categories category);

    /**
     * 更新分类
     */
    int update(Categories category);

    /**
     * 删除分类
     */
    int deleteById(@Param("id") Integer id);

    /**
     * 检查分类是否有子分类
     */
    int countByParentId(@Param("parent_id") Integer parent_id);
}
