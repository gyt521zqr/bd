package com.magao.backend.module.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.magao.backend.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色实体类
 * <p>
 * 对应数据库表：sys_role
 * 定义系统中的角色信息
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class Role extends BaseEntity {

    /** 角色编码（如：STUDENT, TEACHER, ADMIN） */
    private String roleCode;

    /** 角色名称 */
    private String roleName;

    /** 角色描述 */
    private String description;

    /** 排序号 */
    private Integer sortOrder;

    /** 状态（0-禁用，1-启用） */
    private Integer status;
}
