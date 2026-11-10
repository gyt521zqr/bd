package com.magao.backend.common.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果封装类
 * <p>
 * 所有Controller接口的返回值统一使用此类封装，包含：
 * - code：响应状态码（200成功，其他为错误码）
 * - message：响应消息
 * - data：响应数据（泛型，支持任意类型）
 * - timestamp：响应时间戳
 * </p>
 * <p>
 * 使用示例：
 * - 成功：Result.success(data)
 * - 失败：Result.fail("操作失败")
 * </p>
 *
 * @param <T> 响应数据类型
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 响应状态码 */
    private Integer code;

    /** 响应消息 */
    private String message;

    /** 响应数据 */
    private T data;

    /** 响应时间戳 */
    private Long timestamp;

    /**
     * 私有构造方法，防止外部直接实例化
     */
    private Result() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 全参构造方法
     *
     * @param code    状态码
     * @param message 消息
     * @param data    数据
     */
    private Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功响应（无数据）
     *
     * @param <T> 数据类型
     * @return 成功响应结果
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    /**
     * 成功响应（带数据）
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功响应结果
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /**
     * 成功响应（带消息和数据）
     *
     * @param message 响应消息
     * @param data    响应数据
     * @param <T>     数据类型
     * @return 成功响应结果
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    /**
     * 失败响应（带消息）
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 失败响应结果
     */
    public static <T> Result<T> fail(String message) {
        return new Result<>(500, message, null);
    }

    /**
     * 失败响应（带错误码和消息）
     *
     * @param code    错误码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 失败响应结果
     */
    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 失败响应（使用错误码枚举）
     *
     * @param errorCode 错误码枚举
     * @param <T>       数据类型
     * @return 失败响应结果
     */
    public static <T> Result<T> fail(ErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    /**
     * 判断响应是否成功
     *
     * @return true表示成功，false表示失败
     */
    public boolean isSuccess() {
        return this.code != null && this.code == 200;
    }
}
