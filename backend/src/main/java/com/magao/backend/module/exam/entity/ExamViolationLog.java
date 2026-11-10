package com.magao.backend.module.exam.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 考试违规日志实体类
 * <p>
 * 对应数据库表：exam_violation_log
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@TableName("exam_violation_log")
public class ExamViolationLog implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    /** 考试记录ID */
    private Long recordId;
    /** 用户ID */
    private Long userId;
    /** 违规类型（1-切屏，2-离开页面，3-复制粘贴，4-其他） */
    private Integer violationType;
    /** 违规描述 */
    private String description;
    private LocalDateTime violationTime;
}
