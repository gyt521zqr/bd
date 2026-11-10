package com.magao.backend.module.cms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.magao.backend.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Banner实体类
 * <p>
 * 对应数据库表：cms_banner
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cms_banner")
public class Banner extends BaseEntity {
    /** 标题 */
    private String title;
    /** 图片地址 */
    private String imageUrl;
    /** 跳转链接 */
    private String linkUrl;
    /** 位置：home-首页等 */
    private String position;
    /** 排序号 */
    private Integer sortOrder;
    /** 状态：0-禁用，1-启用 */
    private Integer status;
}
