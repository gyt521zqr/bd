package com.magao.backend.module.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.magao.backend.common.dto.PageDTO;
import com.magao.backend.module.message.entity.Message;
import com.magao.backend.module.message.mapper.MessageMapper;
import com.magao.backend.module.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 消息服务实现类
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;

    @Override
    public void sendMessage(Long receiverId, String title, String content, Integer messageType) {
        Message message = new Message();
        message.setReceiverId(receiverId);
        message.setSenderId(0L);
        message.setTitle(title);
        message.setContent(content);
        message.setMessageType(messageType);
        message.setIsRead(0);
        messageMapper.insert(message);
        log.info("消息发送成功：receiverId={}, title={}", receiverId, title);
    }

    @Override
    public Page<Message> listMessages(PageDTO pageDTO, Long userId, Integer isRead) {
        Page<Message> page = new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize());
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getReceiverId, userId);
        if (isRead != null) { wrapper.eq(Message::getIsRead, isRead); }
        wrapper.orderByDesc(Message::getCreateTime);
        return messageMapper.selectPage(page, wrapper);
    }

    @Override
    public void markRead(Long messageId, Long userId) {
        Message message = messageMapper.selectById(messageId);
        if (message != null) {
            message.setIsRead(1);
            message.setReadTime(LocalDateTime.now());
            messageMapper.updateById(message);
        }
    }

    @Override
    public void markAllRead(Long userId) {
        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Message> uw =
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
        uw.eq(Message::getReceiverId, userId).eq(Message::getIsRead, 0)
                .set(Message::getIsRead, 1).set(Message::getReadTime, LocalDateTime.now());
        messageMapper.update(null, uw);
    }

    @Override
    public void deleteMessage(Long messageId, Long userId) {
        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            return;
        }
        if (!Objects.equals(message.getReceiverId(), userId)) {
            throw new com.magao.backend.common.exception.BusinessException(
                    com.magao.backend.common.result.ErrorCode.FORBIDDEN, "只能删除自己的消息");
        }
        messageMapper.deleteById(messageId);
    }

    @Override
    public Long getUnreadCount(Long userId) {
        return messageMapper.selectCount(new LambdaQueryWrapper<Message>()
                .eq(Message::getReceiverId, userId).eq(Message::getIsRead, 0));
    }
}
