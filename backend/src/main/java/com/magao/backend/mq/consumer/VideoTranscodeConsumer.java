package com.magao.backend.mq.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.magao.backend.common.constant.RabbitMQConstant;
import com.magao.backend.common.config.VodConfig;
import com.magao.backend.module.vod.service.VodService;
import com.magao.backend.module.vod.vo.VodPlayInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 视频转码消费者
 * <p>
 * 监听视频转码队列。转码由 VOD 云端完成，这里通过查询播放信息确认转码状态；
 * 未启用 VOD 时明确记录不可用（不假装转码成功）。
 * 期望消息：{"videoId":"..."} 或纯 videoId 字符串
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mq.enabled", havingValue = "true", matchIfMissing = false)
public class VideoTranscodeConsumer {

    private final ObjectProvider<VodService> vodServiceProvider;
    private final VodConfig vodConfig;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitMQConstant.VIDEO_TRANSCODE_QUEUE)
    public void handleVideoTranscode(String message) {
        if (message == null || message.isBlank()) return;
        String videoId = parseVideoId(message);
        if (videoId == null) {
            log.warn("视频转码消息格式无效：{}", message);
            return;
        }
        if (!vodConfig.isEnabled()) {
            log.warn("VOD 未启用，跳过转码状态核对：videoId={}", videoId);
            return;
        }
        VodService svc = vodServiceProvider.getIfAvailable();
        if (svc == null) {
            log.warn("VodService 不可用，跳过转码状态核对：videoId={}", videoId);
            return;
        }
        try {
            VodPlayInfoVO info = svc.getPlayInfo(videoId);
            log.info("视频转码状态：videoId={}, status={}", videoId, info == null ? "unknown" : info.getStatus());
        } catch (Exception e) {
            log.error("视频转码状态查询失败：videoId={}", videoId, e);
            throw e; // 交回 MQ 重试
        }
    }

    private String parseVideoId(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            JsonNode vid = node.get("videoId");
            return vid == null ? null : vid.asText();
        } catch (Exception ignored) {
            return message.trim(); // 兼容纯字符串 videoId
        }
    }
}
