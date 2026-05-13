package com.weep.controller.admin;

import com.weep.entity.Carousel;
import com.weep.response.Result;
import com.weep.service.CarouselService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 轮播图管理控制器
 *
 * <p>负责处理轮播图的增删改查操作，所有接口需要管理员权限。
 *
 * <p>主要功能：
 * <ul>
 *   <li>查询所有轮播图</li>
 *   <li>获取轮播图详情</li>
 *   <li>创建轮播图</li>
 *   <li>更新轮播图</li>
 *   <li>更新轮播图状态</li>
 *   <li>删除轮播图</li>
 * </ul>
 *
 * @author Weep
 * @version 1.0
 */
@RestController
@RequestMapping("/api/admin/carousels")
public class AdminCarouselController {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(AdminCarouselController.class);

    /**
     * 轮播图服务接口
     */
    private final CarouselService carouselService;

    /**
     * 构造函数，依赖注入服务
     *
     * @param carouselService 轮播图服务
     */
    @Autowired
    public AdminCarouselController(CarouselService carouselService) {
        this.carouselService = carouselService;
    }

    /**
     * 获取所有轮播图列表
     *
     * <p>返回所有轮播图记录，按创建时间排序。
     *
     * @return 轮播图列表
     *         <ul>
     *           <li>200: 查询成功</li>
     *         </ul>
     */
    @GetMapping
    public Result<List<Carousel>> list() {
        return Result.success(carouselService.findAll());
    }

    /**
     * 获取轮播图详情
     *
     * <p>根据ID获取轮播图详细信息。
     *
     * @param id 轮播图ID
     * @return 轮播图详情
     *         <ul>
     *           <li>200: 查询成功</li>
     *           <li>404: 轮播图不存在</li>
     *         </ul>
     */
    @GetMapping("/{id}")
    public Result<Carousel> detail(@PathVariable Long id) {
        Carousel c = carouselService.findById(id);
        if (c == null) {
            return Result.error(404, "轮播图不存在");
        }
        return Result.success(c);
    }

    /**
     * 创建轮播图
     *
     * <p>创建新的轮播图记录。
     *
     * @param carousel 轮播图对象，包含：
     *                <ul>
     *                  <li>title: 标题（可选）</li>
     *                  <li>imageUrl: 图片URL（必填）</li>
     *                  <li>linkUrl: 链接URL（可选）</li>
     *                  <li>sortOrder: 排序号（可选）</li>
     *                  <li>status: 状态（可选，默认启用）</li>
     *                </ul>
     * @return 创建结果
     *         <ul>
     *           <li>200: 创建成功</li>
     *           <li>500: 创建失败</li>
     *         </ul>
     */
    @PostMapping
    public Result<Carousel> create(@RequestBody Carousel carousel) {
        return Result.success("创建成功", carouselService.create(carousel));
    }

    /**
     * 更新轮播图
     *
     * <p>更新指定轮播图的信息。
     *
     * @param id       轮播图ID
     * @param carousel 更新后的轮播图对象
     * @return 更新结果
     *         <ul>
     *           <li>200: 更新成功</li>
     *           <li>404: 轮播图不存在</li>
     *         </ul>
     */
    @PutMapping("/{id}")
    public Result<Carousel> update(@PathVariable Long id, @RequestBody Carousel carousel) {
        carousel.setId(id);
        return Result.success("更新成功", carouselService.update(carousel));
    }

    /**
     * 更新轮播图状态
     *
     * <p>单独更新轮播图的启用/禁用状态。
     *
     * @param id     轮播图ID
     * @param carousel 请求体，包含status字段
     * @return 更新结果
     *         <ul>
     *           <li>200: 状态更新成功</li>
     *           <li>404: 轮播图不存在</li>
     *         </ul>
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Carousel carousel) {
        carouselService.updateStatus(id, carousel.getStatus());
        return Result.success("状态更新成功", null);
    }

    /**
     * 删除轮播图
     *
     * <p>删除指定的轮播图记录。
     *
     * @param id 轮播图ID
     * @return 删除结果
     *         <ul>
     *           <li>200: 删除成功</li>
     *           <li>404: 轮播图不存在</li>
     *         </ul>
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        carouselService.delete(id);
        return Result.success("删除成功", null);
    }
}