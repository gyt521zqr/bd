package com.magao.backend.mq.consumer;

import com.magao.backend.common.constant.RabbitMQConstant;
import com.magao.backend.mq.message.ExamSubmitMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 考试提交消费者
 * <p>
 * 监听考试提交队列，异步处理答卷批改
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "mq.enabled", havingValue = "true", matchIfMissing = false)
public class ExamSubmitConsumer {

    @RabbitListener(queues = RabbitMQConstant.EXAM_SUBMIT_QUEUE)
    public void handleExamSubmit(ExamSubmitMessage message) {
        log.info("收到考试提交消息：recordId={}, examId={}", message.getRecordId(), message.getExamId());
        // TODO: 自动批改客观题，标记编程题待人工批改
    }
}
