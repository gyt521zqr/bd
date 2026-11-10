package com.magao.backend.mq.message;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 竞赛判题消息
 * <p>
 * 用于RabbitMQ传输竞赛作品提交的判题请求
 * </p>
 *
 * @author Magao开发团队
 */
@Data
public class CompetitionJudgeMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 竞赛ID */
    private Long competitionId;

    /** 用户ID */
    private Long userId;

    /** Scratch sb3 数据（JSON） */
    private String sb3Data;

    /** 提交IP地址 */
    private String ipAddress;

    /** 提交时间 */
    private LocalDateTime submitTime;
}
