package com.magao.backend.module.payment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.magao.backend.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付订单实体类
 * <p>
 * 对应数据库表：pay_order
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pay_order")
public class PayOrder extends BaseEntity {
    /** 订单号 */
    private String orderNo;
    /** 用户ID */
    private Long userId;
    /** 商品类型（1-课程，2-会员，3-积分商品） */
    private Integer productType;
    /** 商品ID */
    private Long productId;
    /** 商品名称 */
    private String productName;
    /** 订单金额 */
    private BigDecimal amount;
    /** 实付金额 */
    private BigDecimal payAmount;
    /** 支付渠道（1-微信，2-支付宝，3-余额，4-优惠券） */
    private Integer payChannel;
    /** 订单状态（0-待支付，1-已支付，2-已取消，3-退款中，4-已退款，5-已过期） */
    private Integer status;
    /** 支付时间 */
    private LocalDateTime payTime;
    /** 过期时间 */
    private LocalDateTime expireTime;
    /** 第三方交易号 */
    private String tradeNo;
}
