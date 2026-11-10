package com.magao.backend.module.exam.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 考试答案实体类
 * <p>
 * 对应数据库表：exam_answer
 * 记录每道题的作答情况
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@TableName("exam_answer")
public class ExamAnswer implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    /** 考试记录ID */
    private Long recordId;
    /** 题目ID */
    private Long questionId;
    /** 用户答案 */
    private String userAnswer;
    /** 提交代码 */
    private String code;
    /** 编程语言 */
    private Integer language;
    /** 评测状态 */
    private Integer judgeStatus;
    /** 得分 */
    private Integer score;
    /** 是否正确（0-错误，1-正确，2-部分正确） */
    private Integer isCorrect;
    /** 批改状态（0-未批改，1-已批改） */
    private Integer gradeStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
