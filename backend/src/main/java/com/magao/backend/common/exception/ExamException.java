package com.magao.backend.common.exception;

import com.magao.backend.common.result.ErrorCode;
import lombok.Getter;

/**
 * 考试异常类
 * <p>
 * 用于处理考试模块相关的异常，如：
 * - 考试不存在或已结束
 * - 考试时间冲突
 * - 考试已交卷不能重复提交
 * - 违规操作（切屏、离开页面等）
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Getter
public class ExamException extends RuntimeException {

    /** 错误码 */
    private final Integer code;
    /** 错误消息 */
    private final String message;

    /**
     * 根据错误消息构造考试异常
     *
     * @param message 错误消息
     */
    public ExamException(String message) {
        super(message);
        this.code = ErrorCode.EXAM_ERROR.getCode();
        this.message = message;
    }

    /**
     * 根据错误码和错误消息构造考试异常
     *
     * @param code    错误码
     * @param message 错误消息
     */
    public ExamException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 根据错误码枚举构造考试异常
     *
     * @param errorCode 错误码枚举
     */
    public ExamException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }
}
