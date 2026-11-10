package com.magao.backend.mq.producer;

import com.magao.backend.common.constant.RabbitMQConstant;
import com.magao.backend.mq.message.JudgeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 代码评测消息生产者
 * <p>
 * 向评测提交队列发送消息
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "mq.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
public class JudgeProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送代码评测消息
     *
     * @param message 评测消息
     */
    public void sendJudgeMessage(JudgeMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConstant.JUDGE_SUBMIT_EXCHANGE,
                RabbitMQConstant.JUDGE_SUBMIT_ROUTING_KEY,
                message);
        log.info("发送评测消息：recordId={}", message.getRecordId());
    }
}
