package com.magao.backend.mq.consumer;

import com.magao.backend.common.constant.RabbitMQConstant;
import com.magao.backend.mq.message.OrderTimeoutMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 订单超时消费者
 * <p>
 * 监听订单超时队列，自动关闭超时未支付的订单
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "mq.enabled", havingValue = "true", matchIfMissing = false)
public class OrderTimeoutConsumer {

    @RabbitListener(queues = RabbitMQConstant.ORDER_TIMEOUT_QUEUE)
    public void handleOrderTimeout(OrderTimeoutMessage message) {
        log.info("收到订单超时消息：orderId={}, orderNo={}", message.getOrderId(), message.getOrderNo());
        // TODO: 检查订单状态，如果仍为待支付则自动关闭
    }
}
