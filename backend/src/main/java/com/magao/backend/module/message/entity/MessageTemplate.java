package com.magao.backend.module.message.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.magao.backend.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息模板实体类
 * <p>
 * 对应数据库表：message_template
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("message_template")
public class MessageTemplate extends BaseEntity {
    /** 模板编码 */
    private String templateCode;
    /** 模板名称 */
    private String templateName;
    /** 模板内容（支持变量占位符） */
    private String content;
    /** 消息类型 */
    private Integer messageType;
    /** 发送渠道（1-站内信，2-短信，3-邮件） */
    private Integer sendChannel;
    /** 状态（0-禁用，1-启用） */
    private Integer status;
}
