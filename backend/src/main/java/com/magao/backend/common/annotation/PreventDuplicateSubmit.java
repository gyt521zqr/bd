package com.magao.backend.common.annotation;

import java.lang.annotation.*;

/**
 * 防重复提交注解
 * <p>
 * 用于防止用户在短时间内重复提交同一请求。
 * 基于Redis实现，在指定时间窗口内如果检测到相同的请求参数，
 * 则拒绝处理并返回提示信息。
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * &#64;PreventDuplicateSubmit(timeWindow = 10, message = "请勿重复提交")
 * &#64;PostMapping("/submit")
 * public Result submit(...) { ... }
 * </pre>
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PreventDuplicateSubmit {

    /**
     * 防重复提交时间窗口（秒）
     * <p>
     * 在该时间窗口内不允许重复提交
     * </p>
     *
     * @return 时间窗口秒数
     */
    long timeWindow() default 10;

    /**
     * 重复提交时的提示消息
     *
     * @return 提示消息
     */
    String message() default "请勿重复提交";
}
