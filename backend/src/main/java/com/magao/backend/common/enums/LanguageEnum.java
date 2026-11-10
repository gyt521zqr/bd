package com.magao.backend.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 编程语言枚举
 * <p>
 * 定义系统支持的编程语言类型：
 * - SCRATCH_JR：Scratch Jr（适合5-7岁低龄儿童）
 * - SCRATCH3：Scratch 3.0（适合8-12岁青少年）
 * - PYTHON：Python（适合12岁以上）
 * - CPP：C++（适合竞赛和进阶学习）
 * - GOC：GOC（图形化C语言，适合过渡学习）
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Getter
@AllArgsConstructor
public enum LanguageEnum {

    /** Scratch Jr */
    SCRATCH_JR(1, "Scratch Jr", "scratchjr", ".sb3", 71),
    /** Scratch 3.0 */
    SCRATCH3(2, "Scratch 3.0", "scratch3", ".sb3", 71),
    /** Python */
    PYTHON(3, "Python", "python", ".py", 71),
    /** C++ */
    CPP(4, "C++", "cpp", ".cpp", 54),
    /** GOC（图形化C语言，语法兼容 C/C++，判分时按 C++ 编译器尝试验证） */
    GOC(5, "GOC", "goc", ".goc", 54);

    /** 语言值（存储到数据库） */
    @EnumValue
    private final Integer value;
    /** 语言显示名称 */
    private final String displayName;
    /** Judge0语言标识（字符串，兼容历史引用） */
    private final String judge0Id;
    /** 文件扩展名 */
    private final String extension;
    /** Judge0 数字语言 ID（调用 Judge0 判分接口时使用） */
    private final int judge0LanguageId;

    /**
     * 根据语言值获取枚举
     *
     * @param value 语言值
     * @return 对应的语言枚举，未找到返回null
     */
    public static LanguageEnum getByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (LanguageEnum language : values()) {
            if (language.getValue().equals(value)) {
                return language;
            }
        }
        return null;
    }

    /**
     * 根据Judge0语言标识获取枚举
     *
     * @param judge0Id Judge0语言标识
     * @return 对应的语言枚举，未找到返回null
     */
    public static LanguageEnum getByJudge0Id(String judge0Id) {
        if (judge0Id == null) {
            return null;
        }
        for (LanguageEnum language : values()) {
            if (language.getJudge0Id().equals(judge0Id)) {
                return language;
            }
        }
        return null;
    }
}
