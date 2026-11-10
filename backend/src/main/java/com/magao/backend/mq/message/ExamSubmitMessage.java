package com.magao.backend.mq.message;

import lombok.Data;
import java.io.Serializable;

/**
 * 考试提交消息
 * <p>
 * 用于RabbitMQ异步处理考试答卷提交和自动评分
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
public class ExamSubmitMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    /** 考试记录ID */
    private Long recordId;
    /** 考试ID */
    private Long examId;
    /** 用户ID */
    private Long userId;
    /** 试卷ID */
    private Long paperId;
}
