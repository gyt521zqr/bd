package com.magao.backend.module.growth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.magao.backend.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 勋章实体类
 * <p>
 * 对应数据库表：medal
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("medal")
public class Medal extends BaseEntity {
    /** 勋章名称 */
    private String medalName;
    /** 勋章图标URL */
    private String iconUrl;
    /** 勋章描述 */
    private String description;
    /** 获取条件类型（1-学习天数，2-完成课程，3-考试分数，4-积分） */
    private Integer conditionType;
    /** 获取条件值 */
    private Integer conditionValue;
    /** 排序号 */
    private Integer sortOrder;
}
