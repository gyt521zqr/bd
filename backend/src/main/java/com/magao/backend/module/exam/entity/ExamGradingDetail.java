package com.magao.backend.module.exam.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 考试批改详情实体类
 * <p>
 * 对应数据库表：exam_grading_detail
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@TableName("exam_grading_detail")
public class ExamGradingDetail implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long taskId;
    private Long recordId;
    private Long answerId;
    /** 批改得分 */
    private Integer score;
    /** 批改评语 */
    private String comment;
    private LocalDateTime createTime;
}
