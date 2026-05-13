package com.weep.controller;

import com.weep.entity.Address;
import com.weep.entity.User;
import com.weep.response.Result;
import com.weep.service.AddressService;
import com.weep.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收货地址控制器
 */
@RestController
@RequestMapping("/api/address")
public class AddressController {

    private static final Logger logger = LoggerFactory.getLogger(AddressController.class);

    @Autowired
    private AddressService addressService;

    @Autowired
    private UserService userService;

    /**
     * 获取当前用户的所有地址
     */
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT', 'USER')")
    public Result<List<Address>> getAddressList(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);

        if (userId == null) {
            return Result.error(401, "未认证用户");
        }

        List<Address> addresses = addressService.findByUserId(userId);
        logger.info("查询地址列表成功 - 用户: {}, 数量: {}", userId, addresses.size());
        return Result.success("查询成功", addresses);
    }

    /**
     * 获取默认地址
     */
    @GetMapping("/default")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT', 'USER')")
    public Result<Address> getDefaultAddress(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);

        if (userId == null) {
            return Result.error(401, "未认证用户");
        }

        Address defaultAddress = addressService.findDefaultByUserId(userId);
        if (defaultAddress != null) {
            logger.info("查询默认地址成功 - 用户: {}", userId);
            return Result.success("查询成功", defaultAddress);
        } else {
            logger.info("用户未设置默认地址 - 用户: {}", userId);
            return Result.error(404, "未设置默认地址");
        }
    }

    /**
     * 添加地址
     */
    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT', 'USER')")
    public Result<Address> addAddress(@RequestBody Address address, Authentication authentication) {
        Long userId = getCurrentUserId(authentication);

        if (userId == null) {
            return Result.error(401, "未认证用户");
        }

        // 参数校验
        if (address.getReceiverName() == null || address.getReceiverName().trim().isEmpty()) {
            return Result.error(400, "收货人姓名不能为空");
        }
        if (address.getReceiverPhone() == null || address.getReceiverPhone().trim().isEmpty()) {
            return Result.error(400, "收货人电话不能为空");
        }
        if (address.getProvince() == null || address.getProvince().trim().isEmpty()) {
            return Result.error(400, "省份不能为空");
        }
        if (address.getCity() == null || address.getCity().trim().isEmpty()) {
            return Result.error(400, "城市不能为空");
        }
        if (address.getDistrict() == null || address.getDistrict().trim().isEmpty()) {
            return Result.error(400, "区县不能为空");
        }
        if (address.getDetailAddress() == null || address.getDetailAddress().trim().isEmpty()) {
            return Result.error(400, "详细地址不能为空");
        }

        address.setUserId(userId);
        Address newAddress = addressService.addAddress(address);

        if (newAddress != null) {
            logger.info("地址添加成功 - 用户: {}, 地址ID: {}", userId, newAddress.getId());
            return Result.success("添加成功", newAddress);
        } else {
            logger.error("地址添加失败 - 用户: {}", userId);
            return Result.error(500, "添加失败");
        }
    }

    /**
     * 更新地址
     */
    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT', 'USER')")
    public Result<Void> updateAddress(@PathVariable Long id,
                                      @RequestBody Address address,
                                      Authentication authentication) {
        Long userId = getCurrentUserId(authentication);

        if (userId == null) {
            return Result.error(401, "未认证用户");
        }

        // 验证地址所有权
        Address existingAddress = addressService.findById(id);
        if (existingAddress == null) {
            return Result.error(404, "地址不存在");
        }
        if (!existingAddress.getUserId().equals(userId)) {
            logger.warn("无权修改地址 - 用户: {}, 地址ID: {}", userId, id);
            return Result.error(403, "无权修改此地址");
        }

        // 参数校验
        if (address.getReceiverName() == null || address.getReceiverName().trim().isEmpty()) {
            return Result.error(400, "收货人姓名不能为空");
        }
        if (address.getReceiverPhone() == null || address.getReceiverPhone().trim().isEmpty()) {
            return Result.error(400, "收货人电话不能为空");
        }
        if (address.getProvince() == null || address.getProvince().trim().isEmpty()) {
            return Result.error(400, "省份不能为空");
        }
        if (address.getCity() == null || address.getCity().trim().isEmpty()) {
            return Result.error(400, "城市不能为空");
        }
        if (address.getDistrict() == null || address.getDistrict().trim().isEmpty()) {
            return Result.error(400, "区县不能为空");
        }
        if (address.getDetailAddress() == null || address.getDetailAddress().trim().isEmpty()) {
            return Result.error(400, "详细地址不能为空");
        }

        address.setId(id);
        address.setUserId(userId);

        boolean success = addressService.updateAddress(address);
        if (success) {
            logger.info("地址更新成功 - 用户: {}, 地址ID: {}", userId, id);
            return Result.success("更新成功", null);
        } else {
            logger.error("地址更新失败 - 用户: {}, 地址ID: {}", userId, id);
            return Result.error(500, "更新失败");
        }
    }

    /**
     * 删除地址
     */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT', 'USER')")
    public Result<Void> deleteAddress(@PathVariable Long id, Authentication authentication) {
        Long userId = getCurrentUserId(authentication);

        if (userId == null) {
            return Result.error(401, "未认证用户");
        }

        // 验证地址所有权
        Address existingAddress = addressService.findById(id);
        if (existingAddress == null) {
            return Result.error(404, "地址不存在");
        }
        if (!existingAddress.getUserId().equals(userId)) {
            logger.warn("无权删除地址 - 用户: {}, 地址ID: {}", userId, id);
            return Result.error(403, "无权删除此地址");
        }

        boolean success = addressService.deleteById(id);
        if (success) {
            logger.info("地址删除成功 - 用户: {}, 地址ID: {}", userId, id);
            return Result.success("删除成功", null);
        } else {
            logger.error("地址删除失败 - 用户: {}, 地址ID: {}", userId, id);
            return Result.error(500, "删除失败");
        }
    }

    /**
     * 设置默认地址
     */
    @PutMapping("/set-default/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MERCHANT', 'USER')")
    public Result<Void> setDefaultAddress(@PathVariable Long id, Authentication authentication) {
        Long userId = getCurrentUserId(authentication);

        if (userId == null) {
            return Result.error(401, "未认证用户");
        }

        // 验证地址所有权
        Address existingAddress = addressService.findById(id);
        if (existingAddress == null) {
            return Result.error(404, "地址不存在");
        }
        if (!existingAddress.getUserId().equals(userId)) {
            logger.warn("无权设置默认地址 - 用户: {}, 地址ID: {}", userId, id);
            return Result.error(403, "无权操作此地址");
        }

        boolean success = addressService.setDefaultAddress(userId, id);
        if (success) {
            logger.info("设置默认地址成功 - 用户: {}, 地址ID: {}", userId, id);
            return Result.success("设置成功", null);
        } else {
            logger.error("设置默认地址失败 - 用户: {}, 地址ID: {}", userId, id);
            return Result.error(500, "设置失败");
        }
    }

    /**
     * 获取当前用户ID
     *
     * @param authentication Spring Security 认证对象
     * @return 用户ID，如果未认证则返回 null
     */
    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            logger.warn("获取用户ID失败：未认证用户");
            return null;
        }

        String phone = authentication.getName();
        User user = userService.findByPhone(phone);

        if (user == null) {
            logger.warn("获取用户ID失败：用户不存在 - 手机号: {}", phone);
            return null;
        }

        return user.getId();
    }
}
