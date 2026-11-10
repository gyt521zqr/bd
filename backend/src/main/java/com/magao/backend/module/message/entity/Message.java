package com.magao.backend.module.message.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.magao.backend.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息实体类
 * <p>
 * 对应数据库表：message
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("message")
public class Message extends BaseEntity {
    /** 接收用户ID */
    private Long receiverId;
    /** 发送用户ID（0表示系统消息） */
    private Long senderId;
    /** 消息标题 */
    private String title;
    /** 消息内容 */
    private String content;
    /** 消息类型（1-系统通知，2-课程通知，3-考试通知，4-互动消息） */
    private Integer messageType;
    /** 是否已读（0-未读，1-已读） */
    private Integer isRead;
    /** 阅读时间 */
    private java.time.LocalDateTime readTime;
}
