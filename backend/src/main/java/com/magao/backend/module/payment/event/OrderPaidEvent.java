package com.magao.backend.module.payment.event;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单支付成功事件。
 * <p>
 * 在 {@code OrderServiceImpl.payOrder} 将订单标记为已支付后发布，
 * 由业务模块（课程购买→入学记录、会员开通等）监听处理，
 * 从而让支付模块与具体业务模块解耦，并保证支付成功后业务数据的一致性。
 * </p>
 *
 * @author Magao开发团队
 * @since 2026-11-08
 */
@Getter
public class OrderPaidEvent {

    /** 订单ID */
    private final Long orderId;

    /** 订单号 */
    private final String orderNo;

    /** 用户ID */
    private final Long userId;

    /** 商品类型（1-课程，2-会员，3-积分商品） */
    private final Integer productType;

    /** 商品ID（课程ID / 会员套餐ID / 积分商品ID） */
    private final Long productId;

    /** 实付金额 */
    private final BigDecimal payAmount;

    /** 支付时间 */
    private final LocalDateTime payTime;

    public OrderPaidEvent(Long orderId, String orderNo, Long userId, Integer productType,
                          Long productId, BigDecimal payAmount, LocalDateTime payTime) {
        this.orderId = orderId;
        this.orderNo = orderNo;
        this.userId = userId;
        this.productType = productType;
        this.productId = productId;
        this.payAmount = payAmount;
        this.payTime = payTime;
    }
}
