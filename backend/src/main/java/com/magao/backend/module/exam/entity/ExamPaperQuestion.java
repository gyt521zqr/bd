package com.magao.backend.module.exam.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 试卷题目关联实体类
 * <p>
 * 对应数据库表：exam_paper_question
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@TableName("exam_paper_question")
public class ExamPaperQuestion implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    /** 试卷ID */
    private Long paperId;
    /** 题目ID */
    private Long questionId;
    /** 题目类型 */
    private Integer questionType;
    /** 分值 */
    private Integer score;
    /** 排序号 */
    private Integer sortOrder;
    private LocalDateTime createTime;
}
