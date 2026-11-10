package com.magao.backend.module.exam.dto;

import lombok.Data;

/**
 * 考试答案保存 DTO
 * <p>
 * 前端逐题/批量提交答案时使用，一次请求携带同一张试卷的多题作答。
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
public class ExamAnswerSaveDTO {
    /** 题目ID */
    private Long questionId;
    /** 用户答案（选择题 A/B/C/D、填空题文本、判断题 true/false） */
    private String userAnswer;
    /** 提交代码（编程题/Scratch题） */
    private String code;
    /** 编程语言编码（1-Python 2-C++ 3-GoC 4-Scratch 5-ScratchJr 6-PythonTurtle） */
    private Integer language;
}
