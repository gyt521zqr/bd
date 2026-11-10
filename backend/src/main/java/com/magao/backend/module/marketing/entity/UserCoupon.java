package com.magao.backend.module.marketing.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户优惠券实体类
 * <p>
 * 对应数据库表：user_coupon
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@TableName("user_coupon")
public class UserCoupon implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long userId;
    private Long couponId;
    /** 使用状态（0-未使用，1-已使用，2-已过期） */
    private Integer status;
    /** 使用时间 */
    private LocalDateTime useTime;
    /** 使用的订单ID */
    private Long orderId;
    private LocalDateTime createTime;
}
