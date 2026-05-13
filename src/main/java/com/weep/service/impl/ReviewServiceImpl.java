package com.weep.service.impl;

import com.weep.entity.Review;
import com.weep.mapper.ReviewMapper;
import com.weep.service.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品评价服务实现类
 *
 * <p>实现商品评价的管理功能，包括评价的添加、查询、追加和删除操作。
 * 使用 @Transactional 注解保证事务一致性。
 */
@Service
public class ReviewServiceImpl implements ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewServiceImpl.class);

    @Autowired
    private ReviewMapper reviewMapper;

    /**
     * 添加商品评价
     *
     * <p>校验规则：
     * <ul>
     *   <li>评分必须在1-5之间</li>
     *   <li>同一订单项只能评价一次</li>
     * </ul>
     *
     * @param userId 用户ID
     * @param orderItemId 订单项ID
     * @param productId 商品ID
     * @param rating 评分（1-5）
     * @param content 评价内容
     * @param images 评价图片（逗号分隔）
     * @return 创建成功的评价对象
     * @throws IllegalArgumentException 当评分无效或已评价时抛出
     */
    @Override
    @Transactional
    public Review addReview(Long userId, Long orderItemId, String productId, Integer rating, String content, String images) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("评分必须在1-5之间");
        }
        List<Review> existing = reviewMapper.findByOrderItemId(orderItemId);
        if (!existing.isEmpty()) {
            throw new IllegalArgumentException("该订单项已评价");
        }
        Review review = new Review();
        review.setUserId(userId);
        review.setOrderItemId(orderItemId);
        review.setProductId(productId);
        review.setRating(rating);
        review.setContent(content);
        review.setImages(images);
        reviewMapper.insert(review);
        logger.info("评价创建成功 - ID: {}, 商品: {}, 评分: {}", review.getId(), productId, rating);
        return review;
    }

    /**
     * 根据商品ID查询评价列表
     *
     * @param productId 商品ID
     * @return 评价列表
     */
    @Override
    public List<Review> findByProductId(String productId) {
        return reviewMapper.findByProductId(productId);
    }

    /**
     * 根据用户ID查询评价列表
     *
     * @param userId 用户ID
     * @return 评价列表
     */
    @Override
    public List<Review> findByUserId(Long userId) {
        return reviewMapper.findByUserId(userId);
    }

    /**
     * 根据ID查询评价
     *
     * @param id 评价ID
     * @return 评价对象，不存在则返回null
     */
    @Override
    public Review findById(Long id) {
        return reviewMapper.findById(id);
    }

    /**
     * 追加评价内容
     *
     * @param id 评价ID
     * @param additionalContent 追加的评价内容
     */
    @Override
    @Transactional
    public void addAdditionalReview(Long id, String additionalContent) {
        reviewMapper.updateAdditional(id, additionalContent);
        logger.info("追加评价成功 - ID: {}", id);
    }

    /**
     * 删除评价
     *
     * @param id 评价ID
     */
    @Override
    @Transactional
    public void deleteReview(Long id) {
        reviewMapper.deleteById(id);
        logger.info("评价删除成功 - ID: {}", id);
    }

    /**
     * 获取商品评分统计
     *
     * @param productId 商品ID
     * @return 包含平均评分和评价数量的Map
     */
    @Override
    public Map<String, Object> getProductRating(String productId) {
        Map<String, Object> result = new HashMap<>();
        result.put("averageRating", reviewMapper.getAvgRatingByProductId(productId));
        result.put("reviewCount", reviewMapper.countByProductId(productId));
        return result;
    }
}