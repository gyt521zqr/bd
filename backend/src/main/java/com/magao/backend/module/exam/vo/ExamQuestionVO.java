package com.magao.backend.module.exam.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 试卷题目视图对象（题库单源，P1b 新增）
 * <p>
 * 与前端 ExamQuestion 契约对齐：options 为数组、templateCode 为字符串。
 * </p>
 *
 * @author Magao开发团队
 * @since 2026-01-01
 */
@Data
public class ExamQuestionVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    /** 题型: choice/fill/judge/code/scratch/scratchjr */
    private String type;
    /** 题干 */
    private String title;
    /** 选项（选择题，数组） */
    private List<String> options;
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
