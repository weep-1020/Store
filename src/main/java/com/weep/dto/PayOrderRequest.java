package com.weep.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 支付订单请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayOrderRequest {

    /**
     * 支付方式：wechat-微信支付，alipay-支付宝，bank-银行转账
     */
    @NotBlank(message = "支付方式不能为空")
    private String payment_method;

    /**
     * 支付金额
     */
    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额必须大于0")
    private Double pay_amount;
}