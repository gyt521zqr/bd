package com.magao.backend.module.payment.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 订单列表项视图对象（对齐前端 OrderItem：字符串枚举）
 */
@Data
public class OrderItemVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String orderNo;
    private Long userId;
    private Long productId;
    private String productName;
    private String productType; // course | membership | goods
    private Double payAmount;
    private String status;       // pending | paid | cancelled | refunded | expired
    private String payMethod;    // wechat | alipay | balance
    private String payTime;
    private String createTime;
}
