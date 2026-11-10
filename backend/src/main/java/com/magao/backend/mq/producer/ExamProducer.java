package com.magao.backend.mq.producer;

import com.magao.backend.common.constant.RabbitMQConstant;
import com.magao.backend.mq.message.ExamSubmitMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 考试消息生产者
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "mq.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
public class ExamProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送考试提交消息
     */
    public void sendExamSubmitMessage(ExamSubmitMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConstant.EXAM_SUBMIT_EXCHANGE,
                RabbitMQConstant.EXAM_SUBMIT_ROUTING_KEY,
                message);
        log.info("发送考试提交消息：recordId={}", message.getRecordId());
    }
}
