package com.weep.entity;

import java.time.LocalDateTime;

public class Review {
    private Long id;
    private Long orderItemId;
    private Long userId;
    private String productId;
    private Integer rating;
    private String content;
    private String images;
    private String additionalContent;
    private LocalDateTime additionalTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String username;

    public Review() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }

    public String getAdditionalContent() { return additionalContent; }
    public void setAdditionalContent(String additionalContent) { this.additionalContent = additionalContent; }

    public LocalDateTime getAdditionalTime() { return additionalTime; }
    public void setAdditionalTime(LocalDateTime additionalTime) { this.additionalTime = additionalTime; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
