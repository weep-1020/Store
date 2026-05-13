package com.weep.dto;

import com.weep.entity.OrderItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量添加订单明细请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchAddOrderItemsRequest {

    /**
     * 订单编号
     */
    @NotBlank(message = "订单编号不能为空")
    private String order_no;

    /**
     * 订单项列表
     */
    @NotEmpty(message = "订单项列表不能为空")
    @Valid
    private List<OrderItem> items;
}