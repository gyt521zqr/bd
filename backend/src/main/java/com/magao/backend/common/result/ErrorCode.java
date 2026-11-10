package com.magao.backend.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举类
 * <p>
 * 统一管理系统中的错误码和错误消息，便于前端根据错误码进行不同处理。
 * 错误码规范：
 * - 1xx：参数校验相关
 * - 2xx：用户相关
 * - 3xx：认证授权相关
 * - 4xx：业务相关
 * - 5xx：系统相关
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // ==================== 通用错误（1xx） ====================

    /** 成功 */
    SUCCESS(200, "操作成功"),
    /** 通用业务错误 */
    BUSINESS_ERROR(400, "操作失败"),
    /** 参数校验失败 */
    PARAM_ERROR(401, "参数校验失败"),
    /** 参数为空 */
    PARAM_EMPTY(402, "参数不能为空"),
    /** 数据格式错误 */
    PARAM_FORMAT_ERROR(403, "数据格式错误"),
    /** 请求过于频繁 */
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后再试"),

    // ==================== 用户相关错误（2xx） ====================

    /** 用户不存在 */
    USER_NOT_FOUND(201, "用户不存在"),
    /** 用户名已存在 */
    USERNAME_EXISTS(202, "用户名已存在"),
    /** 手机号已注册 */
    PHONE_EXISTS(203, "手机号已注册"),
    /** 密码错误 */
    PASSWORD_ERROR(204, "密码错误"),
    /** 密码不能为空 */
    PASSWORD_EMPTY(205, "密码不能为空"),
    /** 旧密码错误 */
    OLD_PASSWORD_ERROR(206, "旧密码错误"),
    /** 用户已被禁用 */
    USER_DISABLED(207, "用户已被禁用"),
    /** 短信验证码错误 */
    SMS_CODE_ERROR(208, "短信验证码错误"),
    /** 短信验证码已过期 */
    SMS_CODE_EXPIRED(209, "短信验证码已过期"),

    // ==================== 认证授权错误（3xx） ====================

    /** 未登录/未认证 */
    UNAUTHORIZED(301, "未登录或Token已过期"),
    /** Token无效 */
    TOKEN_INVALID(302, "Token无效"),
    /** Token已过期 */
    TOKEN_EXPIRED(303, "Token已过期"),
    /** 权限不足 */
    FORBIDDEN(304, "权限不足"),
    /** 账号在其他设备登录 */
    ACCOUNT_LOGIN_ELSEWHERE(305, "账号已在其他设备登录"),
    /** 登录失败次数过多 */
    LOGIN_FAIL_TOO_MANY(306, "登录失败次数过多，请稍后再试"),

    // ==================== 业务相关错误（4xx） ====================

    /** 数据不存在 */
    DATA_NOT_FOUND(401, "数据不存在"),
    /** 数据已存在 */
    DATA_EXISTS(402, "数据已存在"),
    /** 数据状态异常 */
    DATA_STATUS_ERROR(403, "数据状态异常"),
    /** 操作不允许 */
    OPERATION_NOT_ALLOWED(404, "当前状态不允许此操作"),
    /** 文件上传失败 */
    FILE_UPLOAD_ERROR(405, "文件上传失败"),
    /** 文件类型不支持 */
    FILE_TYPE_NOT_ALLOWED(406, "文件类型不支持"),
    /** 文件不存在 */
    FILE_NOT_FOUND(407, "文件不存在"),

    /** 考试错误 */
    EXAM_ERROR(410, "考试操作失败"),
    /** 考试不存在 */
    EXAM_NOT_FOUND(411, "考试不存在"),
    /** 考试未开始 */
    EXAM_NOT_STARTED(412, "考试未开始"),
    /** 考试已结束 */
    EXAM_FINISHED(413, "考试已结束"),
    /** 已交卷不能重复提交 */
    EXAM_ALREADY_SUBMITTED(414, "已交卷，不能重复提交"),
    /** 考试违规 */
    EXAM_VIOLATION(415, "考试违规操作"),
    /** 考试记录不存在 */
    EXAM_RECORD_NOT_FOUND(416, "考试记录不存在"),

    /** 支付错误 */
    PAYMENT_ERROR(420, "支付操作失败"),
    /** 订单不存在 */
    ORDER_NOT_FOUND(421, "订单不存在"),
    /** 订单已过期 */
    ORDER_EXPIRED(422, "订单已过期"),
    /** 余额不足 */
    BALANCE_NOT_ENOUGH(423, "余额不足"),

    // ==================== 系统错误（5xx） ====================

    /** 系统内部错误 */
    SYSTEM_ERROR(500, "系统内部错误"),
    /** 服务不可用 */
    SERVICE_UNAVAILABLE(501, "服务暂不可用"),
    /** 数据库错误 */
    DATABASE_ERROR(502, "数据库操作失败"),
    /** Redis错误 */
    REDIS_ERROR(503, "缓存操作失败"),
    /** MQ消息发送失败 */
    MQ_SEND_ERROR(504, "消息发送失败"),
    /** 外部接口调用失败 */
    EXTERNAL_API_ERROR(505, "外部接口调用失败");

    /** 错误码 */
    private final Integer code;
    /** 错误消息 */
    private final String message;
}
