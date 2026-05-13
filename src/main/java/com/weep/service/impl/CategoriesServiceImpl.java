package com.weep.service.impl;

import com.weep.entity.Categories;
import com.weep.mapper.CategoriesMapper;
import com.weep.service.CategoriesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品分类服务实现类
 *
 * <p>实现商品分类的管理功能，包括分类的增删改查和树形结构构建。
 * 使用 @Transactional 注解保证事务一致性。
 */
@Service
public class CategoriesServiceImpl implements CategoriesService {

    private static final Logger logger = LoggerFactory.getLogger(CategoriesServiceImpl.class);

    @Autowired
    private CategoriesMapper categoriesMapper;

    /**
     * 根据ID查询分类
     *
     * @param id 分类ID
     * @return 分类对象，不存在则返回null
     */
    @Override
    public Categories findById(Integer id) {
        try {
            return categoriesMapper.findById(id);
        } catch (Exception e) {
            logger.error("查询分类失败 - ID: {}, 错误: {}", id, e.getMessage());
            return null;
        }
    }

    /**
     * 查询所有分类
     *
     * @return 分类列表，无数据则返回空列表
     */
    @Override
    public List<Categories> findAll() {
        try {
            return categoriesMapper.findAll();
        } catch (Exception e) {
            logger.error("查询所有分类失败 - 错误: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 查询父分类列表
     *
     * @return 父分类列表（parent_id = 0）
     */
    @Override
    public List<Categories> findParentCategories() {
        try {
            return categoriesMapper.findParentCategories();
        } catch (Exception e) {
            logger.error("查询父分类失败 - 错误: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 根据父ID查询子分类
     *
     * @param parent_id 父分类ID
     * @return 子分类列表
     */
    @Override
    public List<Categories> findByParentId(Integer parent_id) {
        try {
            return categoriesMapper.findByParentId(parent_id);
        } catch (Exception e) {
            logger.error("查询子分类失败 - 父ID: {}, 错误: {}", parent_id, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 获取分类树形结构
     *
     * <p>将平面的分类列表转换为树形结构，方便前端展示。
     *
     * @return 分类树形结构列表
     */
    @Override
    public List<Categories> getCategoryTree() {
        try {
            // 获取所有分类
            List<Categories> allCategories = categoriesMapper.findAll();
            
            // 构建树形结构
            Map<Integer, Categories> categoryMap = new HashMap<>();
            List<Categories> tree = new ArrayList<>();

            // 第一遍：将所有分类放入 Map
            for (Categories category : allCategories) {
                categoryMap.put(category.getId(), category);
            }

            // 第二遍：构建父子关系
            for (Categories category : allCategories) {
                if (category.getParent_id() == 0) {
                    tree.add(category);
                } else {
                    Categories parent = categoryMap.get(category.getParent_id());
                    if (parent != null) {
                        if (parent.getChildren() == null) {
                            parent.setChildren(new ArrayList<>());
                        }
                        parent.getChildren().add(category);
                    }
                }
            }

            return tree;
        } catch (Exception e) {
            logger.error("构建分类树失败 - 错误: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 添加分类
     *
     * @param category 分类对象
     * @return 添加成功返回true，失败返回false
     */
    @Override
    public boolean addCategory(Categories category) {
        try {
            // 设置默认排序
            if (category.getSort_order() == 0) {
                category.setSort_order(1);
            }
            
            int result = categoriesMapper.insert(category);
            if (result > 0) {
                logger.info("分类添加成功 - 名称: {}", category.getCategory_name());
                return true;
            } else {
                logger.error("分类添加失败 - 名称: {}", category.getCategory_name());
                return false;
            }
        } catch (Exception e) {
            logger.error("分类添加异常 - 错误: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 更新分类
     *
     * @param category 分类对象（必须包含id）
     * @return 更新成功返回true，失败返回false
     */
    @Override
    public boolean updateCategory(Categories category) {
        try {
            int result = categoriesMapper.update(category);
            if (result > 0) {
                logger.info("分类更新成功 - ID: {}", category.getId());
                return true;
            } else {
                logger.error("分类更新失败 - ID: {}", category.getId());
                return false;
            }
        } catch (Exception e) {
            logger.error("分类更新异常 - 错误: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 删除分类（如果有子分类则不能删除）
     *
     * @param id 分类ID
     * @return 删除成功返回true，失败返回false
     */
    @Override
    @Transactional
    public boolean deleteById(Integer id) {
        try {
            // 检查是否有子分类
            int childCount = categoriesMapper.countByParentId(id);
            if (childCount > 0) {
                logger.warn("删除分类失败 - ID: {}, 存在 {} 个子分类", id, childCount);
                return false;
            }

            int result = categoriesMapper.deleteById(id);
            if (result > 0) {
                logger.info("分类删除成功 - ID: {}", id);
                return true;
            } else {
                logger.error("分类删除失败 - ID: {}", id);
                return false;
            }
        } catch (Exception e) {
            logger.error("分类删除异常 - 错误: {}", e.getMessage());
            return false;
        }
    }
}