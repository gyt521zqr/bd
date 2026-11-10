package com.magao.backend.common.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * <p>
 * 用于记录Controller方法的操作日志，
 * 自动记录操作人、操作时间、请求参数、返回结果等信息。
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * &#64;LogOperation(module = "用户管理", type = "新增", description = "新增用户")
 * &#64;PostMapping("/add")
 * public Result addUser(...) { ... }
 * </pre>
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogOperation {

    /**
     * 模块名称
     *
     * @return 模块名称
     */
    String module() default "";

    /**
     * 操作类型（如：新增、修改、删除、查询、导出等）
     *
     * @return 操作类型
     */
    String type() default "";

    /**
     * 操作描述
     *
     * @return 操作描述
     */
    String description() default "";

    /**
     * 是否记录请求参数
     *
     * @return true表示记录
     */
    boolean recordParam() default true;

    /**
     * 是否记录返回结果
     *
     * @return true表示记录
     */
    boolean recordResult() default false;
}
