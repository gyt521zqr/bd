package com.magao.backend.module.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户角色关联实体类
 * <p>
 * 对应数据库表：sys_user_role
 * 用户和角色的多对多关联中间表
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@TableName("sys_user_role")
public class UserRole implements Serializable {
    private static final long serialVersionUID = 1L;
    /** 用户ID */
    private Long userId;
    /** 角色ID */
    private Long roleId;
    /** 创建时间 */
    private LocalDateTime createTime;
}
