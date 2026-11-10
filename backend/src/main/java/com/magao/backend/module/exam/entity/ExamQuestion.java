package com.magao.backend.module.exam.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.magao.backend.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 考试题目实体类（题库单源，P1b 新增）
 * <p>
 * 对应数据库表：exam_question
 * options 字段存储 JSON 数组字符串（选择题选项），由 VO 层解析为 List<String>。
 * </p>
 *
 * @author Magao开发团队
 * @since 2026-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_question")
public class ExamQuestion extends BaseEntity {
    /** 题型: choice/fill/judge/code/scratch/scratchjr */
    private String type;
    /** 题干 */
    private String title;
    /** 选项（JSON数组字符串，选择题） */
    private String options;
    /** 正确答案 */
    private String answer;
    /** 解析 */
    private String analysis;
    /** 分值 */
    private Integer score;
    /** 编程语言（代码题/Scratch题） */
    private String language;
    /** 模板代码（代码题） */
    private String templateCode;
}
