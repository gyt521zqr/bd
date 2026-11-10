package com.magao.backend.module.growth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户等级实体类
 * <p>
 * 对应数据库表：user_level
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@TableName("user_level")
public class UserLevel implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long userId;
    /** 等级 */
    private Integer level;
    /** 等级名称 */
    private String levelName;
    /** 当前经验值 */
    private Integer experience;
    /** 升级所需经验值 */
    private Integer nextLevelExp;
    private LocalDateTime updateTime;
}
