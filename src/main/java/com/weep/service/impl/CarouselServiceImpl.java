package com.weep.service.impl;

import com.weep.entity.Carousel;
import com.weep.mapper.CarouselMapper;
import com.weep.service.CarouselService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 轮播图服务实现类
 *
 * <p>实现首页轮播图的管理功能，包括轮播图的增删改查、状态管理等。
 * 使用 @Transactional 注解保证事务一致性。
 */
@Service
public class CarouselServiceImpl implements CarouselService {

    private static final Logger logger = LoggerFactory.getLogger(CarouselServiceImpl.class);

    @Autowired
    private CarouselMapper carouselMapper;

    /**
     * 查询所有启用状态的轮播图
     *
     * @return 启用状态的轮播图列表
     */
    @Override
    public List<Carousel> findActive() {
        return carouselMapper.findActive();
    }

    /**
     * 查询所有轮播图（包含禁用状态）
     *
     * @return 所有轮播图列表
     */
    @Override
    public List<Carousel> findAll() {
        return carouselMapper.findAll();
    }

    /**
     * 根据ID查询轮播图
     *
     * @param id 轮播图ID
     * @return 轮播图实体对象，不存在则返回null
     */
    @Override
    public Carousel findById(Long id) {
        return carouselMapper.findById(id);
    }

    /**
     * 创建轮播图
     *
     * @param carousel 轮播图实体对象
     * @return 创建成功的轮播图对象
     */
    @Override
    @Transactional
    public Carousel create(Carousel carousel) {
        if (carousel.getSortOrder() == null) {
            carousel.setSortOrder(0);
        }
        if (carousel.getStatus() == null) {
            carousel.setStatus(1);
        }
        carouselMapper.insert(carousel);
        logger.info("轮播图创建成功 - ID: {}, 标题: {}", carousel.getId(), carousel.getTitle());
        return carousel;
    }

    /**
     * 更新轮播图信息
     *
     * @param carousel 轮播图实体对象（必须包含id）
     * @return 更新成功的轮播图对象
     */
    @Override
    @Transactional
    public Carousel update(Carousel carousel) {
        carouselMapper.update(carousel);
        logger.info("轮播图更新成功 - ID: {}", carousel.getId());
        return carousel;
    }

    /**
     * 删除轮播图
     *
     * @param id 轮播图ID
     */
    @Override
    @Transactional
    public void delete(Long id) {
        carouselMapper.deleteById(id);
        logger.info("轮播图删除成功 - ID: {}", id);
    }

    /**
     * 更新轮播图状态
     *
     * @param id 轮播图ID
     * @param status 状态（1-启用，0-禁用）
     */
    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        carouselMapper.updateStatus(id, status);
        logger.info("轮播图状态更新 - ID: {}, 状态: {}", id, status);
    }
}