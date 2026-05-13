package com.weep.controller.admin;

import com.weep.dto.DashboardResponse;
import com.weep.response.Result;
import com.weep.service.AdminService;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员仪表盘控制器
 *
 * <p>负责提供管理员仪表盘数据统计接口。
 *
 * <p>主要功能：
 * <ul>
 *   <li>获取仪表盘统计数据（订单数、用户数、商品数、销售额等）</li>
 * </ul>
 *
 * @author Weep
 * @version 1.0
 */
@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    /**
     * 管理员服务接口
     */
    private final AdminService adminService;

    /**
     * 构造函数，依赖注入服务
     *
     * @param adminService 管理员服务
     */
    public AdminDashboardController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * 获取仪表盘统计数据
     *
     * <p>返回系统统计概览数据，包括：
     * <ul>
     *   <li>今日订单数</li>
     *   <li>今日销售额</li>
     *   <li>用户总数</li>
     *   <li>商品总数</li>
     *   <li>订单状态分布</li>
     *   <li>销售趋势数据</li>
     * </ul>
     *
     * @return 仪表盘数据
     *         <ul>
     *           <li>200: 查询成功</li>
     *         </ul>
     */
    @GetMapping
    public Result<DashboardResponse> dashboard() {
        return Result.success(adminService.getDashboard());
    }
}