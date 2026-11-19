package com.magao.backend.module.exam.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 考试答题详情视图对象（成绩页用，逐题解析）
 * <p>
 * 由后端判分结果 + 试卷题目（含标准答案）合并而来，前端不再自行比对标准答案。
 * </p>
 *
 * @author Magao开发团队
 * @since 2026-01-01
 */
@Data
public class ExamResultDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 题目ID */
    private Long questionId;
    /** 题型 */
    private String type;
    /** 题干 */
    private String title;
    /** 选项（选择题） */
    private List<String> options;
    /** 用户答案 */
    private String userAnswer;
    /** 标准答案 */
    private String correctAnswer;
    /** 解析 */
    private String analysis;
    /** 本题得分 */
    private Integer score;
    /** 本题满分 */
    private Integer maxScore;
    /** 是否答对（客观题）；主观题为空 */
    private Integer isCorrect;
    /** 批改状态（0-待批改 1-已批改） */
    private Integer gradeStatus;
    /** 编程语言（代码题） */
    private String language;
    /** 用户提交的代码 */
    private String code;
}
