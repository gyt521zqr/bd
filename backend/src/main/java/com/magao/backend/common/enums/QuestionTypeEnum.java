package com.magao.backend.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 题目类型枚举
 * <p>
 * 定义考试和练习中支持的题目类型：
 * - SINGLE_CHOICE：单选题
 * - MULTI_CHOICE：多选题
 * - JUDGE：判断题
 * - FILL_BLANK：填空题
 * - SHORT_ANSWER：简答题
 * - PROGRAMMING：编程题
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Getter
@AllArgsConstructor
public enum QuestionTypeEnum {

    /** 单选题 */
    SINGLE_CHOICE(1, "单选题"),
    /** 多选题 */
    MULTI_CHOICE(2, "多选题"),
    /** 判断题 */
    JUDGE(3, "判断题"),
    /** 填空题 */
    FILL_BLANK(4, "填空题"),
    /** 简答题 */
    SHORT_ANSWER(5, "简答题"),
    /** 编程题 */
    PROGRAMMING(6, "编程题");

    /** 类型值（存储到数据库） */
    @EnumValue
    private final Integer value;
    /** 类型描述 */
    private final String description;

    /**
     * 根据类型值获取枚举
     *
     * @param value 类型值
     * @return 对应的类型枚举，未找到返回null
     */
    public static QuestionTypeEnum getByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (QuestionTypeEnum type : values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return null;
    }
}
