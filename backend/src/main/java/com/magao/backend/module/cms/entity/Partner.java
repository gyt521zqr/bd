package com.magao.backend.module.cms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.magao.backend.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 合作伙伴实体类
 * <p>
 * 对应数据库表：cms_partner
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cms_partner")
public class Partner extends BaseEntity {
    /** 合作伙伴名称 */
    private String name;
    /** Logo图片 */
    private String logo;
    /** 官网地址 */
    private String website;
    /** 描述 */
    private String description;
    /** 排序号 */
    private Integer sortOrder;
    /** 状态：0-禁用，1-启用 */
    private Integer status;
}
