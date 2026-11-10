package com.magao.backend.common.exception;

import com.magao.backend.common.result.ErrorCode;
import lombok.Getter;

/**
 * 认证授权异常类
 * <p>
 * 用于处理认证和授权相关的异常，如：
 * - Token无效或已过期
 * - 用户名或密码错误
 * - 用户已被禁用
 * - 权限不足
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Getter
public class AuthException extends RuntimeException {

    /** 错误码 */
    private final Integer code;
    /** 错误消息 */
    private final String message;

    /**
     * 根据错误消息构造认证异常
     *
     * @param message 错误消息
     */
    public AuthException(String message) {
        super(message);
        this.code = ErrorCode.UNAUTHORIZED.getCode();
        this.message = message;
    }

    /**
     * 根据错误码和错误消息构造认证异常
     *
     * @param code    错误码
     * @param message 错误消息
     */
    public AuthException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 根据错误码枚举构造认证异常
     *
     * @param errorCode 错误码枚举
     */
    public AuthException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }
}
