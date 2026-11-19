package com.magao.backend.mq.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.magao.backend.common.constant.RabbitMQConstant;
import com.magao.backend.module.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 消息通知消费者
 * <p>
 * 监听消息通知队列，解析 JSON 消息落站内信。
 * 期望格式：{"receiverId":123,"title":"...","content":"...","messageType":1}
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mq.enabled", havingValue = "true", matchIfMissing = false)
public class MessageNotifyConsumer {

    private final MessageService messageService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitMQConstant.MESSAGE_NOTIFY_QUEUE)
    public void handleMessageNotify(String message) {
        if (message == null || message.isBlank()) return;
        try {
            JsonNode node = objectMapper.readTree(message);
            JsonNode receiverNode = node.get("receiverId");
            if (receiverNode == null || !receiverNode.canConvertToLong()) {
                log.warn("消息通知缺少 receiverId，忽略：{}", message);
                return;
            }
            long receiverId = receiverNode.asLong();
            String title = text(node, "title", "系统通知");
            String content = text(node, "content", "");
            int messageType = node.has("messageType") ? node.get("messageType").asInt(1) : 1;
            messageService.sendMessage(receiverId, title, content, messageType);
            log.info("站内信已落库：receiverId={}, title={}", receiverId, title);
        } catch (Exception e) {
            log.error("消息通知处理失败：{}", message, e);
            throw new RuntimeException("消息通知处理失败", e); // 交回 MQ 重试
        }
    }

    private String text(JsonNode node, String field, String def) {
        JsonNode n = node.get(field);
        return n == null || n.isNull() ? def : n.asText();
    }
}
