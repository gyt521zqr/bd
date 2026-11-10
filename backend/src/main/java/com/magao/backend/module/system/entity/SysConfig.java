package com.magao.backend.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.magao.backend.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统配置实体类
 * <p>
 * 对应数据库表：sys_config
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_config")
public class SysConfig extends BaseEntity {
    /** 配置键 */
    private String configKey;
    /** 配置值 */
    private String configValue;
    /** 配置描述 */
    private String description;
    /** 配置分组 */
    private String configGroup;
}
