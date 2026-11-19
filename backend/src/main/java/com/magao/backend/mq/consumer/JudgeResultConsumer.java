package com.magao.backend.mq.consumer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.magao.backend.common.constant.RabbitMQConstant;
import com.magao.backend.module.exam.entity.ExamAnswer;
import com.magao.backend.module.exam.mapper.ExamAnswerMapper;
import com.magao.backend.module.exam.service.ExamService;
import com.magao.backend.mq.message.JudgeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 代码评测结果消费者
 * <p>
 * 监听代码评测结果队列，把判题得分写回 exam_answer 并重算试卷总分。
 * 注：当前 JudgeController.submit 为同步判题，此消费者是异步兜底通道。
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mq.enabled", havingValue = "true", matchIfMissing = false)
public class JudgeResultConsumer {

    private final ExamAnswerMapper examAnswerMapper;
    private final ExamService examService;

    @RabbitListener(queues = RabbitMQConstant.JUDGE_RESULT_QUEUE)
    public void handleJudgeResult(JudgeMessage message) {
        if (message == null || message.getRecordId() == null || message.getQuestionId() == null) return;
        log.info("收到评测消息：recordId={}, questionId={}", message.getRecordId(), message.getQuestionId());
        try {
            // JudgeMessage 是评测请求体（含代码/语言），无分数；将代码落到对应答卷并标记待批改，
            // 再由 examService 对该记录做一次客观题自动判分兜底（主观题保持待人工批改）。
            LambdaQueryWrapper<ExamAnswer> qw = new LambdaQueryWrapper<ExamAnswer>()
                    .eq(ExamAnswer::getRecordId, message.getRecordId())
                    .eq(ExamAnswer::getQuestionId, message.getQuestionId());
            ExamAnswer answer = examAnswerMapper.selectOne(qw);
            if (answer != null && message.getCode() != null) {
                answer.setCode(message.getCode());
                answer.setLanguage(message.getLanguage());
                answer.setJudgeStatus(0);
                answer.setUpdateTime(LocalDateTime.now());
                examAnswerMapper.updateById(answer);
            }
            examService.autoGradeRecord(message.getRecordId());
            log.info("评测消息已处理：recordId={}, questionId={}",
                    message.getRecordId(), message.getQuestionId());
        } catch (Exception e) {
            log.error("评测消息处理失败：recordId={}, questionId={}",
                    message.getRecordId(), message.getQuestionId(), e);
            throw e;
        }
    }
}
