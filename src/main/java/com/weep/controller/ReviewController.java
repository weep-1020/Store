package com.weep.controller;

import com.weep.entity.Review;
import com.weep.entity.User;
import com.weep.response.Result;
import com.weep.service.ReviewService;
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
@RequestMapping("/api/reviews")
public class ReviewController {

    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    @GetMapping("/product/{productId}")
    public Result<List<Review>> listByProduct(@PathVariable String productId) {
        List<Review> reviews = reviewService.findByProductId(productId);
        return Result.success(reviews);
    }

    @GetMapping("/product/{productId}/rating")
    public Result<Map<String, Object>> getRating(@PathVariable String productId) {
        return Result.success(reviewService.getProductRating(productId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','MERCHANT','USER')")
    public Result<Review> addReview(@RequestBody Map<String, Object> request, Authentication authentication) {
        if (authentication == null) {
            return Result.error(401, "请先登录");
        }
        Long orderItemId = request.get("orderItemId") != null ? Long.valueOf(request.get("orderItemId").toString()) : null;
        String productId = (String) request.get("productId");
        Integer rating = request.get("rating") != null ? Integer.valueOf(request.get("rating").toString()) : null;
        String content = (String) request.get("content");
        String images = (String) request.get("images");

        if (orderItemId == null || productId == null || rating == null) {
            return Result.error(400, "orderItemId, productId, rating 不能为空");
        }

        User user = userService.findByPhone(authentication.getName());
        if (user == null) {
            return Result.error(401, "用户不存在");
        }
        Long userId = user.getId();
        try {
            Review review = reviewService.addReview(userId, orderItemId, productId, rating, content, images);
            return Result.success("评价成功", review);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PutMapping("/{id}/additional")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','MERCHANT','USER')")
    public Result<Void> addAdditional(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String additionalContent = request.get("additionalContent");
        if (additionalContent == null || additionalContent.isBlank()) {
            return Result.error(400, "追加评价内容不能为空");
        }
        reviewService.addAdditionalReview(id, additionalContent);
        return Result.success("追加评价成功", null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','MERCHANT','USER')")
    public Result<Void> delete(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return Result.success("删除成功", null);
    }
}
