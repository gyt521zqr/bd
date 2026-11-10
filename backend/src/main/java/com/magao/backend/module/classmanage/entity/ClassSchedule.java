package com.magao.backend.module.classmanage.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 课程表实体类
 * <p>
 * 对应数据库表：class_schedule
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@TableName("class_schedule")
public class ClassSchedule implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long classId;
    /** 上课日期 */
    private LocalDateTime classDate;
    /** 上课时间段 */
    private String timeSlot;
    /** 主题 */
    private String topic;
    /** 教室 */
    private String classroom;
    private LocalDateTime createTime;
}
