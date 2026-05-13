package com.weep.controller;

import com.weep.entity.Products;
import com.weep.response.PageResult;
import com.weep.response.Result;
import com.weep.service.OrderService;
import com.weep.service.ProductsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/merchant")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT')")
public class MerchantController {

    private static final Logger logger = LoggerFactory.getLogger(MerchantController.class);

    private final ProductsService productsService;
    private final OrderService orderService;

    public MerchantController(ProductsService productsService, OrderService orderService) {
        this.productsService = productsService;
        this.orderService = orderService;
    }

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        try {
            List<Products> all = productsService.findAll();
            Map<String, Object> stats = new HashMap<>();
            stats.put("productCount", all.size());
            stats.put("activeCount", all.stream().filter(p -> "1".equals(p.getStatus())).count());
            stats.put("outOfStockCount", all.stream().filter(p -> p.getStock() != null && p.getStock() == 0).count());
            stats.put("totalOrders", orderService.findAll().size());
            stats.put("pendingDelivery", orderService.findByStatus("1").size());
            return Result.success(stats);
        } catch (Exception e) {
            logger.error("获取仪表盘数据失败", e);
            return Result.error(500, "获取数据失败");
        }
    }

    @GetMapping("/products")
    public Result<PageResult<Products>> listProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 12;
        PageResult<Products> result = productsService.search(keyword, null, null, page, size, "created_at", "DESC");
        return Result.success(result);
    }

    @PostMapping("/products/add")
    public Result<Void> addProduct(@RequestBody Products product) {
        boolean success = productsService.addProduct(product);
        return success ? Result.success("添加成功", null) : Result.error(400, "添加失败");
    }

    @PutMapping("/products/update")
    public Result<Void> updateProduct(@RequestBody Products product) {
        boolean success = productsService.updateProduct(product);
        return success ? Result.success("更新成功", null) : Result.error(400, "更新失败");
    }

    @DeleteMapping("/products/{spid}")
    public Result<Void> deleteProduct(@PathVariable String spid) {
        boolean success = productsService.deleteBySpid(spid);
        return success ? Result.success("删除成功", null) : Result.error(400, "删除失败");
    }

    @PutMapping("/products/{spid}/status")
    public Result<Void> updateStatus(@PathVariable String spid, @RequestBody Map<String, String> request) {
        String status = request.get("status");
        if (status == null || (!status.equals("0") && !status.equals("1"))) {
            return Result.error(400, "状态值无效");
        }
        boolean success = productsService.updateStatus(spid, status);
        return success ? Result.success("状态更新成功", null) : Result.error(400, "更新失败");
    }

    @PutMapping("/products/{spid}/stock")
    public Result<Void> updateStock(@PathVariable String spid, @RequestBody Map<String, Integer> request) {
        Integer stock = request.get("stock");
        if (stock == null || stock < 0) return Result.error(400, "库存参数无效");
        boolean success = productsService.updateStock(spid, stock);
        return success ? Result.success("库存更新成功", null) : Result.error(400, "更新失败");
    }

    @GetMapping("/orders")
    public Result<?> listOrders(@RequestParam(required = false) String status) {
        if (status != null && !status.isEmpty()) {
            return Result.success(orderService.findByStatus(status));
        }
        return Result.success(orderService.findAll());
    }

    @PostMapping("/orders/{orderNo}/deliver")
    public Result<Void> deliverOrder(@PathVariable String orderNo) {
        boolean success = orderService.deliverOrder(orderNo);
        return success ? Result.success("发货成功", null) : Result.error(400, "发货失败");
    }
}
