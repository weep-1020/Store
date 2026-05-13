package com.weep.service;

import com.weep.entity.Categories;

import java.util.List;

/**
 * 商品分类服务接口
 */
public interface CategoriesService {

    /**
     * 根据ID查询分类
     */
    Categories findById(Integer id);

    /**
     * 查询所有分类
     */
    List<Categories> findAll();

    /**
     * 查询父分类列表
     */
    List<Categories> findParentCategories();

    /**
     * 根据父ID查询子分类
     */
    List<Categories> findByParentId(Integer parent_id);

    /**
     * 获取分类树形结构
     */
    List<Categories> getCategoryTree();

    /**
     * 添加分类
     */
    boolean addCategory(Categories category);

    /**
     * 更新分类
     */
    boolean updateCategory(Categories category);

    /**
     * 删除分类（如果有子分类则不能删除）
     */
    boolean deleteById(Integer id);
}
