package com.magao.backend.module.cms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.magao.backend.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分类实体类
 * <p>
 * 对应数据库表：cms_category
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cms_category")
public class Category extends BaseEntity {
    /** 分类名称 */
    private String name;
    /** 分类类型：news-资讯，course-课程，practice-练习 */
    private String type;
    /** 父分类ID */
    private Long parentId;
    /** 排序号 */
    private Integer sortOrder;
    /** 状态：0-禁用，1-启用 */
    private Integer status;
}
