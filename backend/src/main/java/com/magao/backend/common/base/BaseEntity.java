package com.magao.backend.common.base;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体基类
 * <p>
 * 所有实体类的父类，包含公共字段：
 * - id：主键ID（自增）
 * - createTime：创建时间（自动填充）
 * - updateTime：更新时间（自动填充）
 * - deleted：逻辑删除标识（0-未删除，1-已删除）
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID（自增） */
    private Long id;

    /** 创建时间（插入时自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间（插入和更新时自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除标识（0-未删除，1-已删除） */
    @TableLogic
    private Integer deleted;
}
