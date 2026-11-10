package com.magao.backend.mq.consumer;

import com.magao.backend.common.constant.RabbitMQConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 视频转码消费者
 * <p>
 * 监听视频转码队列，异步处理上传视频的转码任务
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "mq.enabled", havingValue = "true", matchIfMissing = false)
public class VideoTranscodeConsumer {

    @RabbitListener(queues = RabbitMQConstant.VIDEO_TRANSCODE_QUEUE)
    public void handleVideoTranscode(String message) {
        log.info("收到视频转码消息：{}", message);
        // TODO: 调用FFmpeg进行视频转码
    }
}
