package com.magao.backend.mq.message;

import lombok.Data;
import java.io.Serializable;

/**
 * 订单超时消息
 * <p>
 * 用于RabbitMQ延迟队列处理超时未支付的订单
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
public class OrderTimeoutMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    /** 订单ID */
    private Long orderId;
    /** 订单号 */
    private String orderNo;
    /** 用户ID */
    private Long userId;
}
