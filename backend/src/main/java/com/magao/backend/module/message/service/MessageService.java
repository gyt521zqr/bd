package com.magao.backend.module.message.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.magao.backend.common.dto.PageDTO;
import com.magao.backend.module.message.entity.Message;

/**
 * 消息服务接口
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
public interface MessageService {

    /** 发送消息 */
    void sendMessage(Long receiverId, String title, String content, Integer messageType);

    /** 获取用户消息列表 */
    Page<Message> listMessages(PageDTO pageDTO, Long userId, Integer isRead);

    /** 标记消息已读 */
    void markRead(Long messageId, Long userId);

    /** 标记全部消息已读 */
    void markAllRead(Long userId);

    /** 删除消息（仅本人） */
    void deleteMessage(Long messageId, Long userId);

    /** 获取未读消息数 */
    Long getUnreadCount(Long userId);
}
