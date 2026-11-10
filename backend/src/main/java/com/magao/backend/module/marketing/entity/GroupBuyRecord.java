package com.magao.backend.module.marketing.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 团购记录实体类
 * <p>
 * 对应数据库表：group_buy_record
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@TableName("group_buy_record")
public class GroupBuyRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long activityId;
    private Long userId;
    /** 团长用户ID */
    private Long leaderId;
    /** 团购组号 */
    private String groupNo;
    /** 参团状态（0-待成团，1-已成团，2-已过期） */
    private Integer status;
    private LocalDateTime createTime;
}
