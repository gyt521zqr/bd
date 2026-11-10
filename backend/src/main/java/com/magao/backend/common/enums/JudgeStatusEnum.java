package com.magao.backend.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 代码评测状态枚举
 * <p>
 * 定义代码评测任务的状态：
 * - PENDING：待评测（已提交，等待评测引擎处理）
 * - RUNNING：评测中（评测引擎正在执行代码）
 * - ACCEPTED：通过（所有测试用例通过）
 * - WRONG_ANSWER：答案错误（部分或全部测试用例未通过）
 * - TIME_LIMIT_EXCEEDED：时间超限（运行时间超过限制）
 * - MEMORY_LIMIT_EXCEEDED：内存超限（内存使用超过限制）
 * - RUNTIME_ERROR：运行错误（代码执行过程中发生错误）
 * - COMPILE_ERROR：编译错误（代码编译失败）
 * - SYSTEM_ERROR：系统错误（评测系统内部错误）
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Getter
@AllArgsConstructor
public enum JudgeStatusEnum {

    /** 待评测 */
    PENDING(0, "待评测"),
    /** 评测中 */
    RUNNING(1, "评测中"),
    /** 通过 */
    ACCEPTED(2, "通过"),
    /** 答案错误 */
    WRONG_ANSWER(3, "答案错误"),
    /** 时间超限 */
    TIME_LIMIT_EXCEEDED(4, "时间超限"),
    /** 内存超限 */
    MEMORY_LIMIT_EXCEEDED(5, "内存超限"),
    /** 运行错误 */
    RUNTIME_ERROR(6, "运行错误"),
    /** 编译错误 */
    COMPILE_ERROR(7, "编译错误"),
    /** 系统错误 */
    SYSTEM_ERROR(8, "系统错误");

    /** 状态值（存储到数据库） */
    @EnumValue
    private final Integer value;
    /** 状态描述 */
    private final String description;

    /**
     * 根据状态值获取枚举
     *
     * @param value 状态值
     * @return 对应的状态枚举，未找到返回null
     */
    public static JudgeStatusEnum getByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (JudgeStatusEnum status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
