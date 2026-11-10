package com.magao.backend.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 考试状态枚举
 * <p>
 * 定义考试的生命周期状态：
 * - NOT_STARTED：未开始（考试已创建但未到开始时间）
 * - IN_PROGRESS：进行中（考试正在进行）
 * - FINISHED：已结束（考试时间已到）
 * - GRADED：已批改（所有答卷已批改完成）
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Getter
@AllArgsConstructor
public enum ExamStatusEnum {

    /** 未开始 */
    NOT_STARTED(0, "未开始"),
    /** 进行中 */
    IN_PROGRESS(1, "进行中"),
    /** 已结束 */
    FINISHED(2, "已结束"),
    /** 已批改 */
    GRADED(3, "已批改");

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
    public static ExamStatusEnum getByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (ExamStatusEnum status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
