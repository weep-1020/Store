package com.weep.mapper;

import com.weep.entity.Review;
import org.apache.ibatis.annotations.Param;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ReviewMapper {
    int insert(Review review);
    Review findById(@Param("id") Long id);
    List<Review> findByProductId(@Param("productId") String productId);
    List<Review> findByUserId(@Param("userId") Long userId);
    List<Review> findByOrderItemId(@Param("orderItemId") Long orderItemId);
    int updateAdditional(@Param("id") Long id, @Param("additionalContent") String additionalContent);
    int deleteById(@Param("id") Long id);
    Double getAvgRatingByProductId(@Param("productId") String productId);
    int countByProductId(@Param("productId") String productId);
}
