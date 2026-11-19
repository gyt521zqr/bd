package com.magao.backend.mq.consumer;

import com.magao.backend.common.constant.RabbitMQConstant;
import com.magao.backend.module.payment.service.OrderService;
import com.magao.backend.mq.message.OrderTimeoutMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 订单超时消费者
 * <p>
 * 监听订单超时队列，幂等关闭仍待支付的订单并释放锁定的优惠券。
 * 只信任 orderNo（不信任消息里的 userId，防伪造）。
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mq.enabled", havingValue = "true", matchIfMissing = false)
public class OrderTimeoutConsumer {

    private final OrderService orderService;

    @RabbitListener(queues = RabbitMQConstant.ORDER_TIMEOUT_QUEUE)
    public void handleOrderTimeout(OrderTimeoutMessage message) {
        if (message == null || message.getOrderNo() == null) return;
        log.info("收到订单超时消息：orderId={}, orderNo={}", message.getOrderId(), message.getOrderNo());
        try {
            boolean changed = orderService.closeExpiredOrder(message.getOrderNo());
            log.info("订单超时处理结果：orderNo={}, closed={}", message.getOrderNo(), changed);
        } catch (Exception e) {
            log.error("订单超时关单失败：orderNo={}", message.getOrderNo(), e);
            throw e; // 交回 MQ 重试
        }
    }
}
