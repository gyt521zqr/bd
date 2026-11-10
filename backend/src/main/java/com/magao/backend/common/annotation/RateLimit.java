package com.magao.backend.common.annotation;

import java.lang.annotation.*;

/**
 * 限流注解
 * <p>
 * 用于接口级别的限流控制，基于Redis实现滑动窗口限流算法。
 * 可标注在Controller方法上，限制指定时间窗口内的最大请求数。
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * &#64;RateLimit(key = "login", timeWindow = 60, maxCount = 5)
 * &#64;PostMapping("/login")
 * public Result login(...) { ... }
 * </pre>
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流Key前缀（用于区分不同接口）
     * <p>
     * 实际Redis Key格式：rate:limit:{key}:{ip或userId}
     * </p>
     *
     * @return Key前缀
     */
    String key() default "";

    /**
     * 时间窗口（秒）
     * <p>
     * 在该时间窗口内统计请求数
     * </p>
     *
     * @return 时间窗口秒数
     */
    long timeWindow() default 60;

    /**
     * 时间窗口内最大请求数
     *
     * @return 最大请求数
     */
    int maxCount() default 10;

    /**
     * 限流提示消息
     *
     * @return 提示消息
     */
    String message() default "请求过于频繁，请稍后再试";
}
