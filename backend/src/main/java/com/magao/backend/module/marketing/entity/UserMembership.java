package com.magao.backend.module.marketing.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户会员权益实体（P0-04：付费开通会员后生效）
 */
@Data
@TableName("user_membership")
public class UserMembership implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long userId;
    private Long planId;
    private Long orderId;
    private LocalDateTime startTime;
    private LocalDateTime expireTime;
    /** 0-已过期 1-生效中 */
    private Integer status;
    private LocalDateTime createTime;
}
