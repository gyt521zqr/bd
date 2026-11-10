package com.magao.backend.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.magao.backend.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据字典实体类
 * <p>
 * 对应数据库表：sys_dict
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict")
public class SysDict extends BaseEntity {
    /** 字典类型 */
    private String dictType;
    /** 字典名称 */
    private String dictName;
    /** 状态（0-停用，1-启用） */
    private Integer status;
    /** 备注 */
    private String remark;
}
