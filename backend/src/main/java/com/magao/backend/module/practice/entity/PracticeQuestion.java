package com.magao.backend.module.practice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.magao.backend.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 练习题实体类
 * <p>
 * 对应数据库表：practice_question
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("practice_question")
public class PracticeQuestion extends BaseEntity {
    /** 题目标题 */
    private String title;
    /** 题目描述 */
    private String description;
    /** 题目类型（1-编程题） */
    private Integer questionType;
    /** 编程语言 */
    private Integer language;
    /** 难度等级（1-简单，2-中等，3-困难） */
    private Integer difficulty;
    /** 时间限制（秒） */
    private Integer timeLimit;
    /** 内存限制（MB） */
    private Integer memoryLimit;
    /** 模板代码 */
    private String templateCode;
    /** 参考答案 */
    private String answer;
    /** 提示信息 */
    private String hint;
    /** 样例输入输出（JSON 数组字符串：[{input,output}]） */
    private String examples;
    /** 通过率 */
    private java.math.BigDecimal passRate;
    /** 提交次数 */
    private Integer submitCount;
    /** 通过次数 */
    private Integer passCount;
}
