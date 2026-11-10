package com.magao.backend.mq.producer;

import com.magao.backend.common.constant.RabbitMQConstant;
import com.magao.backend.mq.message.OrderTimeoutMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 订单消息生产者
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "mq.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
public class OrderProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送订单超时检查消息
     */
    public void sendOrderTimeoutMessage(OrderTimeoutMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConstant.ORDER_TIMEOUT_EXCHANGE,
                RabbitMQConstant.ORDER_TIMEOUT_ROUTING_KEY,
                message);
        log.info("发送订单超时消息：orderId={}", message.getOrderId());
    }
}
