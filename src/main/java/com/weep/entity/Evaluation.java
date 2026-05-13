package com.weep.entity;

import java.time.LocalDateTime;

/**
 * 商品评价实体类
 * <p>
 * 用于表示系统中的商品评价信息
 * </p>
 *
 * @author Store Team
 * @version 1.0
 */
public class Evaluation {

    /**
     * 评价ID
     */
    private Long id;

    /**
     * 用户ID（关联User表）
     */
    private Long userId;

    /**
     * 商品编号（关联Products表）
     */
    private String pid;

    /**
     * 评分（1-5星）
     */
    private Integer rating;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 评价图片URL列表（JSON格式存储）
     */
    private String imageUrls;

    /**
     * 评价状态（1-显示，0-隐藏）
     */
    private Integer status;

    /**
     * 点赞数
     */
    private Integer likes;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 默认构造函数
     */
    public Evaluation() {
    }

    /**
     * 带参数的构造函数
     *
     * @param id           评价ID
     * @param userId       用户ID
     * @param pid          商品编号
     * @param rating       评分
     * @param content      评价内容
     * @param imageUrls    评价图片URL列表
     * @param status       评价状态
     * @param likes        点赞数
     * @param createdAt    创建时间
     * @param updatedAt    更新时间
     */
    public Evaluation(Long id, Long userId, String pid, Integer rating, String content,
                      String imageUrls, Integer status, Integer likes,
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.pid = pid;
        this.rating = rating;
        this.content = content;
        this.imageUrls = imageUrls;
        this.status = status;
        this.likes = likes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(String imageUrls) {
        this.imageUrls = imageUrls;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }



    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Evaluation{" +
                "id=" + id +
                ", userId=" + userId +
                ", pid='" + pid + '\'' +
                ", rating=" + rating +
                ", content='" + content + '\'' +
                ", imageUrls='" + imageUrls + '\'' +
                ", status=" + status +
                ", likes=" + likes +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
