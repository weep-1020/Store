package com.weep.service;

import com.weep.entity.Review;
import java.util.List;
import java.util.Map;

/**
 * 评价服务接口
 *
 * <p>提供商品评价的管理功能，包括评价的添加、查询、追加评论、删除等操作。
 */
public interface ReviewService {

    /**
     * 添加商品评价
     *
     * @param userId 用户ID
     * @param orderItemId 订单项ID
     * @param productId 商品ID
     * @param rating 评分（1-5星）
     * @param content 评价内容
     * @param images 评价图片（逗号分隔的URL）
     * @return 添加成功的评价对象
     */
    Review addReview(Long userId, Long orderItemId, String productId, Integer rating, String content, String images);

    /**
     * 根据商品ID查询评价列表
     *
     * @param productId 商品ID
     * @return 该商品的评价列表
     */
    List<Review> findByProductId(String productId);

    /**
     * 根据用户ID查询评价列表
     *
     * @param userId 用户ID
     * @return 该用户的评价列表
     */
    List<Review> findByUserId(Long userId);

    /**
     * 根据ID查询评价
     *
     * @param id 评价ID
     * @return 评价对象，不存在则返回null
     */
    Review findById(Long id);

    /**
     * 追加评论
     *
     * @param id 评价ID
     * @param additionalContent 追加内容
     */
    void addAdditionalReview(Long id, String additionalContent);

    /**
     * 删除评价
     *
     * @param id 评价ID
     */
    void deleteReview(Long id);

    /**
     * 获取商品评分统计
     *
     * @param productId 商品ID
     * @return 包含平均分、评价数量、各星级分布的Map
     */
    Map<String, Object> getProductRating(String productId);
}