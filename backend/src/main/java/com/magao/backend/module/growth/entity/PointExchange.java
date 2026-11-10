package com.magao.backend.module.growth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 积分兑换记录实体类
 * <p>
 * 对应数据库表：point_exchange
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@TableName("point_exchange")
public class PointExchange implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long userId;
    private Long goodsId;
    /** 消耗积分 */
    private Integer costPoint;
    /** 兑换状态（0-待发货，1-已发货，2-已完成） */
    private Integer status;
    private LocalDateTime createTime;
}
