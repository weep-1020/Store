package com.weep.controller;

import com.weep.entity.CartItem;
import com.weep.entity.User;
import com.weep.response.Result;
import com.weep.service.CartService;
import com.weep.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    /**
     * 获取当前用户的购物车列表
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT', 'USER')")
    public Result<List<CartItem>> getCart(Authentication authentication) {
        Long userId = getUserId(authentication);
        if (userId == null) return Result.error(401, "未认证");

        List<CartItem> items = cartService.findByUserId(userId);
        return Result.success("查询成功", items);
    }

    /**
     * 添加到购物车
     */
    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT', 'USER')")
    public Result<CartItem> addToCart(@RequestBody Map<String, Object> request, Authentication authentication) {
        Long userId = getUserId(authentication);
        if (userId == null) return Result.error(401, "未认证");

        String skuId = (String) request.get("skuId");
        if (skuId == null) {
            skuId = (String) request.get("spid");
        }
        Integer quantity = request.get("quantity") != null ? ((Number) request.get("quantity")).intValue() : 1;

        if (skuId == null || skuId.isBlank()) {
            return Result.error(400, "商品SKU不能为空");
        }

        CartItem item = cartService.addToCart(userId, skuId, quantity);
        if (item != null) {
            return Result.success("添加成功", item);
        } else {
            return Result.error(400, "添加失败，商品不存在或参数无效");
        }
    }

    /**
     * 更新购物车项数量
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT', 'USER')")
    public Result<Void> updateQuantity(@PathVariable Long id, @RequestBody Map<String, Integer> request, Authentication authentication) {
        Long userId = getUserId(authentication);
        if (userId == null) return Result.error(401, "未认证");

        Integer quantity = request.get("quantity");
        if (quantity == null || quantity <= 0) {
            return Result.error(400, "数量必须大于0");
        }

        boolean success = cartService.updateQuantity(userId, id, quantity);
        if (success) {
            return Result.success("更新成功", null);
        } else {
            return Result.error(400, "更新失败");
        }
    }

    /**
     * 删除购物车项
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT', 'USER')")
    public Result<Void> removeItem(@PathVariable Long id, Authentication authentication) {
        Long userId = getUserId(authentication);
        if (userId == null) return Result.error(401, "未认证");

        boolean success = cartService.removeById(userId, id);
        if (success) {
            return Result.success("删除成功", null);
        } else {
            return Result.error(400, "删除失败");
        }
    }

    /**
     * 清空购物车
     */
    @DeleteMapping("/clear")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT', 'USER')")
    public Result<Void> clearCart(Authentication authentication) {
        Long userId = getUserId(authentication);
        if (userId == null) return Result.error(401, "未认证");

        cartService.clearCart(userId);
        return Result.success("已清空", null);
    }

    /**
     * 批量删除购物车项
     */
    @PostMapping("/batch-remove")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT', 'USER')")
    public Result<Void> batchRemove(@RequestBody Map<String, List<Long>> request, Authentication authentication) {
        Long userId = getUserId(authentication);
        if (userId == null) return Result.error(401, "未认证");

        List<Long> ids = request.get("ids");
        boolean success = cartService.batchRemove(userId, ids);
        if (success) {
            return Result.success("删除成功", null);
        } else {
            return Result.error(400, "删除失败");
        }
    }

    private Long getUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) return null;
        User user = userService.findByPhone(authentication.getName());
        return user != null ? user.getId() : null;
    }
}
