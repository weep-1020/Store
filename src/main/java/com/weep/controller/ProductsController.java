package com.weep.controller;

import com.weep.entity.Products;
import com.weep.response.PageResult;
import com.weep.response.Result;
import com.weep.service.ProductsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商品控制器
 *
 * <p>负责处理商品相关的 REST API 请求，包括商品的增删改查操作。
 * 集成了 Spring Security 权限控制，确保只有授权用户才能执行敏感操作。
 *
 * <p>主要功能：
 * <ul>
 *   <li>商品查询（按 ID、名称、分类、状态）</li>
 *   <li>商品添加（需要 SUPER_ADMIN 或 MERCHANT 权限）</li>
 *   <li>商品更新（需要 SUPER_ADMIN 或 MERCHANT 权限）</li>
 *   <li>商品删除（仅 SUPER_ADMIN 权限）</li>
 *   <li>库存管理（更新库存、扣减库存）</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/products")
public class ProductsController {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(ProductsController.class);

    /**
     * 商品服务接口
     * 提供商品相关的业务逻辑
     */
    private final ProductsService productsService;

    /**
     * 构造函数，依赖注入商品服务
     *
     * @param productsService 商品服务
     */
    public ProductsController(ProductsService productsService) {
        this.productsService = productsService;
    }

    /**
     * 根据商品 ID 查询商品
     *
     * @param spid 商品唯一标识 ID
     * @return 商品信息
     *         <ul>
     *           <li>200: 查询成功，data 包含商品信息</li>
     *           <li>404: 商品不存在</li>
     *         </ul>
     */
    @GetMapping("/{spid}")
    public Result<Products> getProductBySpid(@PathVariable String spid) {
        Products product = productsService.findBySpid(spid);
        if (product != null) {
            return Result.success("查询成功", product);
        } else {
            return Result.error(404, "商品不存在");
        }
    }

    /**
     * 根据商品名称查询商品
     *
     * @param p_name 商品名称
     * @return 商品信息
     *         <ul>
     *           <li>200: 查询成功，data 包含商品信息</li>
     *           <li>404: 商品不存在</li>
     *         </ul>
     */
    @GetMapping("/name/{p_name}")
    public Result<Products> getProductByName(@PathVariable String p_name) {
        Products product = productsService.findByPName(p_name);
        if (product != null) {
            return Result.success("查询成功", product);
        } else {
            return Result.error(404, "商品不存在");
        }
    }

    /**
     * 查询所有商品
     *
     * @return 商品列表
     *         <ul>
     *           <li>200: 查询成功，data 包含商品列表</li>
     *         </ul>
     */
    @GetMapping("/all")
    public Result<List<Products>> getAllProducts() {
        List<Products> products = productsService.findAll();
        return Result.success("查询成功", products);
    }

    /**
     * 根据分类 ID 查询商品列表
     *
     * @param category_id 分类 ID
     * @return 该分类下的商品列表
     *         <ul>
     *           <li>200: 查询成功，data 包含商品列表</li>
     *         </ul>
     */
    @GetMapping("/category/{category_id}")
    public Result<List<Products>> getProductsByCategory(@PathVariable String category_id) {
        List<Products> products = productsService.findByCategoryId(category_id);
        return Result.success("查询成功", products);
    }

    /**
     * 根据状态查询商品列表
     *
     * @param status 商品状态（0: 下架，1: 上架）
     * @return 该状态下的商品列表
     *         <ul>
     *           <li>200: 查询成功，data 包含商品列表</li>
     *         </ul>
     */
    @GetMapping("/status/{status}")
    public Result<List<Products>> getProductsByStatus(@PathVariable String status) {
        List<Products> products = productsService.findByStatus(status);
        return Result.success("查询成功", products);
    }

    /**
     * 搜索商品（支持关键词、分类过滤、分页、排序）
     * 公开访问
     */
    @GetMapping("/search")
    public Result<PageResult<Products>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category_id,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder) {
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 12;
        if (status == null || status.isEmpty()) status = "1";
        PageResult<Products> result = productsService.search(keyword, category_id, status, page, size, sortBy, sortOrder);
        return Result.success("查询成功", result);
    }

    /**
     * 添加商品
     *
     * <p>需要 SUPER_ADMIN 或 MERCHANT 角色权限。
     *
     * @param product 商品信息对象，包含以下字段：
     *                <ul>
     *                  <li>p_name: 商品名称（必填）</li>
     *                  <li>price: 商品价格（必填，≥0）</li>
     *                  <li>stock: 商品库存（必填，≥0）</li>
     *                  <li>category_id: 分类 ID（可选）</li>
     *                  <li>pic: 商品图片 ID（可选）</li>
     *                  <li>description: 商品描述（可选）</li>
     *                </ul>
     * @return 添加结果
     *         <ul>
     *           <li>200: 添加成功</li>
     *           <li>400: 添加失败（名称重复、参数无效等）</li>
     *           <li>403: 无权访问</li>
     *         </ul>
     */
    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT')")
    public Result<Void> addProduct(@RequestBody Products product) {
        boolean success = productsService.addProduct(product);
        if (success) {
            logger.info("商品添加成功 - 名称: {}", product.getP_name());
            return Result.success("商品添加成功", null);
        } else {
            logger.warn("商品添加失败 - 名称: {}", product.getP_name());
            return Result.error(400, "商品添加失败");
        }
    }

    /**
     * 更新商品信息
     *
     * <p>需要 SUPER_ADMIN 或 MERCHANT 角色权限。
     *
     * @param product 商品信息对象，必须包含 spid 字段
     * @return 更新结果
     *         <ul>
     *           <li>200: 更新成功</li>
     *           <li>400: 更新失败（商品不存在、参数无效等）</li>
     *           <li>403: 无权访问</li>
     *         </ul>
     */
    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT')")
    public Result<Void> updateProduct(@RequestBody Products product) {
        boolean success = productsService.updateProduct(product);
        if (success) {
            logger.info("商品更新成功 - ID: {}", product.getSpid());
            return Result.success("商品更新成功", null);
        } else {
            logger.warn("商品更新失败 - ID: {}", product.getSpid());
            return Result.error(400, "商品更新失败");
        }
    }

    /**
     * 删除商品
     *
     * <p>仅 SUPER_ADMIN 角色有权限执行此操作。
     *
     * @param spid 商品唯一标识 ID
     * @return 删除结果
     *         <ul>
     *           <li>200: 删除成功</li>
     *           <li>400: 删除失败（商品不存在等）</li>
     *           <li>403: 无权访问</li>
     *         </ul>
     */
    @DeleteMapping("/{spid}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> deleteProduct(@PathVariable String spid) {
        boolean success = productsService.deleteBySpid(spid);
        if (success) {
            logger.info("商品删除成功 - ID: {}", spid);
            return Result.success("商品删除成功", null);
        } else {
            logger.warn("商品删除失败 - ID: {}", spid);
            return Result.error(400, "商品删除失败");
        }
    }

    /**
     * 更新商品状态
     *
     * <p>需要 SUPER_ADMIN 或 MERCHANT 角色权限。
     *
     * @param spid 商品唯一标识 ID
     * @param request 请求体，包含 status 字段（0: 下架，1: 上架）
     * @return 更新结果
     *         <ul>
     *           <li>200: 更新成功</li>
     *           <li>400: 参数无效或更新失败</li>
     *           <li>403: 无权访问</li>
     *         </ul>
     */
    @PutMapping("/{spid}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT')")
    public Result<Void> updateProductStatus(@PathVariable String spid, @RequestBody Map<String, String> request) {
        String status = request.get("status");
        if (status == null || (!status.equals("0") && !status.equals("1"))) {
            return Result.error(400, "状态值无效，只能是 0 或 1");
        }
        boolean success = productsService.updateStatus(spid, status);
        if (success) {
            logger.info("商品状态更新成功 - ID: {}, 状态: {}", spid, status);
            return Result.success("商品状态更新成功", null);
        } else {
            logger.warn("商品状态更新失败 - ID: {}", spid);
            return Result.error(400, "商品状态更新失败");
        }
    }

    /**
     * 更新商品库存
     *
     * <p>需要 SUPER_ADMIN 或 MERCHANT 角色权限。
     *
     * @param spid 商品唯一标识 ID
     * @param request 请求体，包含 stock 字段（≥0）
     * @return 更新结果
     *         <ul>
     *           <li>200: 更新成功</li>
     *           <li>400: 参数无效或更新失败</li>
     *           <li>403: 无权访问</li>
     *         </ul>
     */
    @PutMapping("/{spid}/stock")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT')")
    public Result<Void> updateProductStock(@PathVariable String spid, @RequestBody Map<String, Integer> request) {
        Integer stock = request.get("stock");
        if (stock == null) {
            return Result.error(400, "库存参数缺失");
        }
        if (stock < 0) {
            return Result.error(400, "库存不能为负数");
        }
        boolean success = productsService.updateStock(spid, stock);
        if (success) {
            logger.info("商品库存更新成功 - ID: {}, 库存: {}", spid, stock);
            return Result.success("商品库存更新成功", null);
        } else {
            logger.warn("商品库存更新失败 - ID: {}", spid);
            return Result.error(400, "商品库存更新失败");
        }
    }

    /**
     * 扣减商品库存
     *
     * <p>需要 SUPER_ADMIN 或 MERCHANT 角色权限。
     * 使用原子操作确保并发安全，防止超卖。
     *
     * @param spid 商品唯一标识 ID
     * @param request 请求体，包含 quantity 字段（>0）
     * @return 扣减结果
     *         <ul>
     *           <li>200: 扣减成功</li>
     *           <li>400: 参数无效或库存不足</li>
     *           <li>403: 无权访问</li>
     *         </ul>
     */
    @PostMapping("/{spid}/stock/decrease")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT')")
    public Result<Void> decreaseStock(@PathVariable String spid, @RequestBody Map<String, Integer> request) {
        Integer quantity = request.get("quantity");
        if (quantity == null) {
            return Result.error(400, "扣减数量参数缺失");
        }
        if (quantity <= 0) {
            return Result.error(400, "扣减数量必须大于0");
        }
        boolean success = productsService.decreaseStock(spid, quantity);
        if (success) {
            logger.info("库存扣减成功 - ID: {}, 扣减数量: {}", spid, quantity);
            return Result.success("库存扣减成功", null);
        } else {
            logger.warn("库存扣减失败 - ID: {}, 扣减数量: {}", spid, quantity);
            return Result.error(400, "库存扣减失败，库存不足或参数错误");
        }
    }
}
