package com.magao.backend.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 课程状态枚举
 * <p>
 * 定义课程的生命周期状态：
 * - DRAFT：草稿（教师创建中，未发布）
 * - PUBLISHED：已发布（学生可见可购买）
 * - OFFLINE：已下架（不可购买，已购买的可继续学习）
 * - ARCHIVED：已归档（不再展示）
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Getter
@AllArgsConstructor
public enum CourseStatusEnum {

    /** 草稿状态 */
    DRAFT(0, "草稿"),
    /** 已发布状态 */
    PUBLISHED(1, "已发布"),
    /** 已下架状态 */
    OFFLINE(2, "已下架"),
    /** 已归档状态 */
    ARCHIVED(3, "已归档");

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
    public static CourseStatusEnum getByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (CourseStatusEnum status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
