package com.weep.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 购物车转订单请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderFromCartRequest {

    /**
     * 用户ID（/my/from-cart 端点从认证信息获取，无需前端传递）
     */
    private Long user_id;

    /**
     * 购物车项ID列表（为空则转全部购物车）
     */
    private List<Long> cartItemIds;

    /**
     * 收货人姓名
     */
    @NotBlank(message = "收货人姓名不能为空")
    private String receiver_name;

    /**
     * 收货人电话
     */
    @NotBlank(message = "收货人电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String receiver_phone;

    /**
     * 收货地址
     */
    @NotBlank(message = "收货地址不能为空")
    private String receiver_address;

    /**
     * 备注
     */
    private String remark;
}