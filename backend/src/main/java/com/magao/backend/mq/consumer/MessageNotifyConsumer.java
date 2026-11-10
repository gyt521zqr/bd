package com.magao.backend.mq.consumer;

import com.magao.backend.common.constant.RabbitMQConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 消息通知消费者
 * <p>
 * 监听消息通知队列，异步发送站内消息、短信、邮件等
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "mq.enabled", havingValue = "true", matchIfMissing = false)
public class MessageNotifyConsumer {

    @RabbitListener(queues = RabbitMQConstant.MESSAGE_NOTIFY_QUEUE)
    public void handleMessageNotify(String message) {
        log.info("收到消息通知：{}", message);
        // TODO: 根据消息类型发送站内信/短信/邮件
    }
}
