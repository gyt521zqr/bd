package com.magao.backend.mq.producer;

import com.magao.backend.common.constant.RabbitMQConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 消息通知生产者
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "mq.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
public class MessageProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送消息通知
     */
    public void sendNotifyMessage(Object message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConstant.MESSAGE_NOTIFY_EXCHANGE,
                "", message);
        log.info("发送消息通知");
    }
}
