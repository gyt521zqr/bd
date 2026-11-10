package com.magao.backend.mq.message;

import lombok.Data;
import java.io.Serializable;

/**
 * 代码评测消息
 * <p>
 * 用于RabbitMQ传输代码评测请求
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
public class JudgeMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    /** 提交记录ID */
    private Long recordId;
    /** 用户ID */
    private Long userId;
    /** 题目ID */
    private Long questionId;
    /** 提交代码 */
    private String code;
    /** 编程语言 */
    private Integer language;
    /** 时间限制（秒） */
    private Integer timeLimit;
    /** 内存限制（MB） */
    private Integer memoryLimit;
}
