package com.magao.backend.module.growth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户勋章实体类
 * <p>
 * 对应数据库表：user_medal
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@TableName("user_medal")
public class UserMedal implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long userId;
    private Long medalId;
    /** 获取时间 */
    private LocalDateTime obtainTime;
}
