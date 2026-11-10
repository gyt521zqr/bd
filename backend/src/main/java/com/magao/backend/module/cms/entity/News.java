package com.magao.backend.module.cms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.magao.backend.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 资讯实体类
 * <p>
 * 对应数据库表：cms_news
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cms_news")
public class News extends BaseEntity {
    /** 标题 */
    private String title;
    /** 摘要 */
    private String summary;
    /** 内容（富文本） */
    private String content;
    /** 封面图 */
    private String coverImage;
    /** 分类ID */
    private Long categoryId;
    /** 作者 */
    private String author;
    /** 来源 */
    private String source;
    /** 浏览量 */
    private Integer viewCount;
    /** 排序号 */
    private Integer sortOrder;
    /** 是否置顶：0-否，1-是 */
    private Integer isPinned;
    /** 状态：0-草稿，1-已发布，2-下架 */
    private Integer status;
    /** 发布时间 */
    private LocalDateTime publishTime;
}
