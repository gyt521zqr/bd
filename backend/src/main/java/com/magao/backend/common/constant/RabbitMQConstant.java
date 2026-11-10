package com.magao.backend.common.constant;

/**
 * RabbitMQ消息队列常量类
 * <p>
 * 统一管理RabbitMQ中所有队列、交换机和路由键的名称，
 * 确保生产者和消费者使用一致的命名。
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
public class RabbitMQConstant {

    /** 私有构造方法，防止实例化 */
    private RabbitMQConstant() {
    }

    // ==================== 代码评测模块 ====================

    /** 代码评测提交队列名称 */
    public static final String JUDGE_SUBMIT_QUEUE = "judge.submit.queue";
    /** 代码评测提交交换机名称 */
    public static final String JUDGE_SUBMIT_EXCHANGE = "judge.submit.exchange";
    /** 代码评测提交路由键 */
    public static final String JUDGE_SUBMIT_ROUTING_KEY = "judge.submit";

    /** 代码评测结果队列名称 */
    public static final String JUDGE_RESULT_QUEUE = "judge.result.queue";
    /** 代码评测结果交换机名称 */
    public static final String JUDGE_RESULT_EXCHANGE = "judge.result.exchange";
    /** 代码评测结果路由键 */
    public static final String JUDGE_RESULT_ROUTING_KEY = "judge.result";

    // ==================== 考试模块 ====================

    /** 考试提交队列名称 */
    public static final String EXAM_SUBMIT_QUEUE = "exam.submit.queue";
    /** 考试提交交换机名称 */
    public static final String EXAM_SUBMIT_EXCHANGE = "exam.submit.exchange";
    /** 考试提交路由键 */
    public static final String EXAM_SUBMIT_ROUTING_KEY = "exam.submit";

    // ==================== 订单模块 ====================

    /** 订单超时队列名称 */
    public static final String ORDER_TIMEOUT_QUEUE = "order.timeout.queue";
    /** 订单超时交换机名称 */
    public static final String ORDER_TIMEOUT_EXCHANGE = "order.timeout.exchange";
    /** 订单超时路由键 */
    public static final String ORDER_TIMEOUT_ROUTING_KEY = "order.timeout";

    // ==================== 视频模块 ====================

    /** 视频转码队列名称 */
    public static final String VIDEO_TRANSCODE_QUEUE = "video.transcode.queue";
    /** 视频转码交换机名称 */
    public static final String VIDEO_TRANSCODE_EXCHANGE = "video.transcode.exchange";
    /** 视频转码路由键 */
    public static final String VIDEO_TRANSCODE_ROUTING_KEY = "video.transcode";

    // ==================== 消息通知模块 ====================

    /** 消息通知队列名称 */
    public static final String MESSAGE_NOTIFY_QUEUE = "message.notify.queue";
    /** 消息通知交换机名称（Fanout广播模式） */
    public static final String MESSAGE_NOTIFY_EXCHANGE = "message.notify.exchange";

    // ==================== 竞赛模块 ====================

    /** 竞赛判题队列名称 */
    public static final String COMPETITION_JUDGE_QUEUE = "competition.judge.queue";
    /** 竞赛判题交换机名称 */
    public static final String COMPETITION_JUDGE_EXCHANGE = "competition.judge.exchange";
    /** 竞赛判题路由键 */
    public static final String COMPETITION_JUDGE_ROUTING_KEY = "competition.judge";
}
