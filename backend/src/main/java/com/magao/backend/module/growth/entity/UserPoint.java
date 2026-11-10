package com.magao.backend.module.growth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户积分实体类
 * <p>
 * 对应数据库表：user_point
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@TableName("user_point")
public class UserPoint implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long userId;
    /** 当前积分 */
    private Integer totalPoint;
    /** 累计获得积分 */
    private Integer totalEarned;
    /** 累计消费积分 */
    private Integer totalSpent;
    private LocalDateTime updateTime;
}
