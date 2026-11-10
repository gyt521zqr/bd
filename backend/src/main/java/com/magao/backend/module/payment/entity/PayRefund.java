package com.magao.backend.module.payment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.magao.backend.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款记录实体类
 * <p>
 * 对应数据库表：pay_refund
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pay_refund")
public class PayRefund extends BaseEntity {
    /** 退款单号 */
    private String refundNo;
    /** 订单ID */
    private Long orderId;
    /** 订单号 */
    private String orderNo;
    /** 用户ID */
    private Long userId;
    /** 退款金额 */
    private BigDecimal refundAmount;
    /** 退款原因 */
    private String reason;
    /** 退款状态（0-待处理，1-已退款，2-已拒绝） */
    private Integer status;
    /** 退款时间 */
    private LocalDateTime refundTime;
    /** 第三方退款单号 */
    private String tradeNo;
}
