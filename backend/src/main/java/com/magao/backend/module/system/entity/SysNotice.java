package com.magao.backend.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.magao.backend.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统公告实体类
 * <p>
 * 对应数据库表：sys_notice
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_notice")
public class SysNotice extends BaseEntity {
    /** 公告标题 */
    private String title;
    /** 公告内容 */
    private String content;
    /** 公告类型（1-通知，2-公告） */
    private Integer noticeType;
    /** 状态（0-关闭，1-开启） */
    private Integer status;
}
