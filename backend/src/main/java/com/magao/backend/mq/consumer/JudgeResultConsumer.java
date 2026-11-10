package com.magao.backend.mq.consumer;

import com.magao.backend.common.constant.RabbitMQConstant;
import com.magao.backend.mq.message.JudgeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 代码评测结果消费者
 * <p>
 * 监听代码评测结果队列，处理评测完成后的回调
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "mq.enabled", havingValue = "true", matchIfMissing = false)
public class JudgeResultConsumer {

    /**
     * 处理代码评测结果
     *
     * @param message 评测结果消息
     */
    @RabbitListener(queues = RabbitMQConstant.JUDGE_RESULT_QUEUE)
    public void handleJudgeResult(JudgeMessage message) {
        log.info("收到评测结果：recordId={}, questionId={}", message.getRecordId(), message.getQuestionId());
        // TODO: 更新评测结果到数据库
    }
}
