package com.magao.backend.common.exception;

import com.magao.backend.common.result.ErrorCode;
import lombok.Getter;

/**
 * 支付异常类
 * <p>
 * 用于处理支付模块相关的异常，如：
 * - 订单不存在或已过期
 * - 支付金额不正确
 * - 支付渠道不可用
 * - 退款失败
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Getter
public class PaymentException extends RuntimeException {

    /** 错误码 */
    private final Integer code;
    /** 错误消息 */
    private final String message;

    /**
     * 根据错误消息构造支付异常
     *
     * @param message 错误消息
     */
    public PaymentException(String message) {
        super(message);
        this.code = ErrorCode.PAYMENT_ERROR.getCode();
        this.message = message;
    }

    /**
     * 根据错误码和错误消息构造支付异常
     *
     * @param code    错误码
     * @param message 错误消息
     */
    public PaymentException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 根据错误码枚举构造支付异常
     *
     * @param errorCode 错误码枚举
     */
    public PaymentException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }
}
