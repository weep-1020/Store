package com.weep.controller;

import com.weep.entity.Categories;
import com.weep.response.Result;
import com.weep.service.CategoriesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类控制器
 *
 * <p>负责处理商品分类相关的 REST API 请求
 */
@RestController
@RequestMapping("/api/categories")
public class CategoriesController {

    private static final Logger logger = LoggerFactory.getLogger(CategoriesController.class);

    private final CategoriesService categoriesService;

    public CategoriesController(CategoriesService categoriesService) {
        this.categoriesService = categoriesService;
    }

    /**
     * 查询所有分类
     */
    @GetMapping("/all")
    public Result<List<Categories>> getAllCategories() {
        List<Categories> categories = categoriesService.findAll();
        return Result.success("查询成功", categories);
    }

    /**
     * 查询父分类列表
     */
    @GetMapping("/parents")
    public Result<List<Categories>> getParentCategories() {
        List<Categories> categories = categoriesService.findParentCategories();
        return Result.success("查询成功", categories);
    }

    /**
     * 根据父ID查询子分类
     */
    @GetMapping("/children/{parent_id}")
    public Result<List<Categories>> getChildCategories(@PathVariable Integer parent_id) {
        List<Categories> categories = categoriesService.findByParentId(parent_id);
        return Result.success("查询成功", categories);
    }

    /**
     * 获取分类树形结构
     */
    @GetMapping("/tree")
    public Result<List<Categories>> getCategoryTree() {
        List<Categories> tree = categoriesService.getCategoryTree();
        return Result.success("查询成功", tree);
    }

    /**
     * 根据ID查询分类
     */
    @GetMapping("/{id}")
    public Result<Categories> getCategoryById(@PathVariable Integer id) {
        Categories category = categoriesService.findById(id);
        if (category != null) {
            return Result.success("查询成功", category);
        } else {
            return Result.error(404, "分类不存在");
        }
    }

    /**
     * 添加分类（需要管理员权限）
     */
    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT')")
    public Result<Void> addCategory(@RequestBody Categories category) {
        boolean success = categoriesService.addCategory(category);
        if (success) {
            logger.info("分类添加成功 - 名称: {}", category.getCategory_name());
            return Result.success("分类添加成功", null);
        } else {
            logger.warn("分类添加失败 - 名称: {}", category.getCategory_name());
            return Result.error(400, "分类添加失败");
        }
    }

    /**
     * 更新分类（需要管理员权限）
     */
    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT')")
    public Result<Void> updateCategory(@RequestBody Categories category) {
        boolean success = categoriesService.updateCategory(category);
        if (success) {
            logger.info("分类更新成功 - ID: {}", category.getId());
            return Result.success("分类更新成功", null);
        } else {
            logger.warn("分类更新失败 - ID: {}", category.getId());
            return Result.error(400, "分类更新失败");
        }
    }

    /**
     * 删除分类（需要管理员权限）
     */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> deleteCategory(@PathVariable Integer id) {
        boolean success = categoriesService.deleteById(id);
        if (success) {
            logger.info("分类删除成功 - ID: {}", id);
            return Result.success("分类删除成功", null);
        } else {
            logger.warn("分类删除失败 - ID: {}", id);
            return Result.error(400, "分类删除失败，可能存在子分类");
        }
    }
}
