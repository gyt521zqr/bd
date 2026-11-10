package com.magao.backend.module.exam.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 考试快照实体类
 * <p>
 * 对应数据库表：exam_snapshot
 * 用于防作弊监控
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@TableName("exam_snapshot")
public class ExamSnapshot implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long recordId;
    private Long userId;
    /** 快照类型（1-屏幕截图，2-摄像头拍照） */
    private Integer snapshotType;
    /** 快照URL */
    private String snapshotUrl;
    private LocalDateTime createTime;
}
