package com.magao.backend.module.exam.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 批改试卷请求
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
public class ExamGradeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 考试记录ID */
    private Long recordId;

    /** 题目ID */
    private Long questionId;

    /** 得分 */
    private Integer score;

    /** 批改评语（前端可传，后端暂作透传，落库按需扩展） */
    private String comment;
}
