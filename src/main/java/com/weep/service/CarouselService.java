package com.weep.service;

import com.weep.entity.Carousel;
import java.util.List;

/**
 * 轮播图服务接口
 *
 * <p>提供首页轮播图的管理功能，包括轮播图的增删改查、状态管理等。
 * 支持轮播图的启用/禁用状态切换。
 */
public interface CarouselService {

    /**
     * 查询所有启用状态的轮播图
     *
     * @return 启用状态的轮播图列表
     */
    List<Carousel> findActive();

    /**
     * 查询所有轮播图（包含禁用状态）
     *
     * @return 所有轮播图列表
     */
    List<Carousel> findAll();

    /**
     * 根据ID查询轮播图
     *
     * @param id 轮播图ID
     * @return 轮播图实体对象，不存在则返回null
     */
    Carousel findById(Long id);

    /**
     * 创建轮播图
     *
     * @param carousel 轮播图实体对象
     * @return 创建成功的轮播图对象
     */
    Carousel create(Carousel carousel);

    /**
     * 更新轮播图信息
     *
     * @param carousel 轮播图实体对象（必须包含id）
     * @return 更新成功的轮播图对象
     */
    Carousel update(Carousel carousel);

    /**
     * 删除轮播图
     *
     * @param id 轮播图ID
     */
    void delete(Long id);

    /**
     * 更新轮播图状态
     *
     * @param id 轮播图ID
     * @param status 状态（1-启用，0-禁用）
     */
    void updateStatus(Long id, Integer status);
}