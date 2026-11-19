package com.magao.backend.mq.consumer;

import com.magao.backend.common.constant.RabbitMQConstant;
import com.magao.backend.module.exam.service.ExamService;
import com.magao.backend.mq.message.ExamSubmitMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 考试提交消费者
 * <p>
 * 监听考试提交队列，异步批改客观题（幂等，重复消息不会重复计分）。
 * 交卷接口已同步判分，此消费者作为异步兜底/重试通道。
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mq.enabled", havingValue = "true", matchIfMissing = false)
public class ExamSubmitConsumer {

    private final ExamService examService;

    @RabbitListener(queues = RabbitMQConstant.EXAM_SUBMIT_QUEUE)
    public void handleExamSubmit(ExamSubmitMessage message) {
        if (message == null || message.getRecordId() == null) return;
        log.info("收到考试提交消息：recordId={}, examId={}", message.getRecordId(), message.getExamId());
        try {
            examService.autoGradeRecord(message.getRecordId());
        } catch (Exception e) {
            log.error("考试自动判分失败：recordId={}", message.getRecordId(), e);
            throw e; // 交回 MQ 重试
        }
    }
}
