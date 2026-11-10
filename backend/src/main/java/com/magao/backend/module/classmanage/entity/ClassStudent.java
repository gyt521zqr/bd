package com.magao.backend.module.classmanage.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 班级学生关联实体类
 * <p>
 * 对应数据库表：class_student
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@TableName("class_student")
public class ClassStudent implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long classId;
    private Long userId;
    /** 加入时间 */
    private LocalDateTime joinTime;
    /** 状态（0-正常，1-已退出） */
    private Integer status;
}
