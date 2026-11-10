package com.magao.backend.module.growth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 积分日志实体类
 * <p>
 * 对应数据库表：point_log
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@TableName("point_log")
public class PointLog implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long userId;
    /** 积分变动值（正数增加，负数减少） */
    private Integer changePoint;
    /** 变动后积分 */
    private Integer afterPoint;
    /** 变动类型（1-签到，2-学习，3-考试，4-兑换，5-管理员调整） */
    private Integer changeType;
    /** 变动描述 */
    private String description;
    /** 关联业务ID */
    private Long bizId;
    private LocalDateTime createTime;
}
