package com.magao.backend.common.exception;

import com.magao.backend.common.result.ErrorCode;
import lombok.Getter;

/**
 * 业务异常类
 * <p>
 * 用于处理业务逻辑中的异常情况，如：
 * - 参数校验失败
 * - 业务规则不满足
 * - 数据不存在或状态不正确
 * </p>
 * <p>
 * 使用方式：throw new BusinessException("用户名已存在")
 * 或：throw new BusinessException(ErrorCode.USER_NOT_FOUND)
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 错误码 */
    private final Integer code;
    /** 错误消息 */
    private final String message;

    /**
     * 根据错误消息构造业务异常
     *
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);
        this.code = ErrorCode.BUSINESS_ERROR.getCode();
        this.message = message;
    }

    /**
     * 根据错误码和错误消息构造业务异常
     *
     * @param code    错误码
     * @param message 错误消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 根据错误码枚举构造业务异常
     *
     * @param errorCode 错误码枚举
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    /**
     * 根据错误码枚举和自定义消息构造业务异常
     *
     * @param errorCode 错误码枚举
     * @param message   自定义错误消息
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
        this.message = message;
    }
}
