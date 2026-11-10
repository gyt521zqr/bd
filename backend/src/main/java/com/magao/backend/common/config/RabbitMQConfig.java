package com.magao.backend.common.config;

import com.magao.backend.common.constant.RabbitMQConstant;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ消息队列配置类
 * <p>
 * 功能说明：
 * 1. 定义消息转换器（JSON格式）
 * 2. 定义队列、交换机和绑定关系
 * 3. 配置消息确认机制
 * </p>
 * <p>
 * 队列定义：
 * - judge.submit.queue：代码评测提交队列
 * - judge.result.queue：代码评测结果队列
 * - exam.submit.queue：考试提交队列
 * - order.timeout.queue：订单超时队列
 * - video.transcode.queue：视频转码队列
 * - message.notify.queue：消息通知队列
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Configuration
@ConditionalOnProperty(name = "mq.enabled", havingValue = "true", matchIfMissing = false)
public class RabbitMQConfig {

    // ==================== 消息转换器 ====================

    /**
     * JSON消息转换器
     * <p>
     * 将Java对象序列化为JSON格式发送到RabbitMQ，
     * 接收时将JSON反序列化为Java对象。
     * </p>
     *
     * @return Jackson2JsonMessageConverter实例
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 配置RabbitTemplate
     * <p>
     * 设置消息转换器和发布确认
     * </p>
     *
     * @param connectionFactory RabbitMQ连接工厂
     * @return 配置好的RabbitTemplate实例
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        // 开启发布确认
        rabbitTemplate.setMandatory(true);
        return rabbitTemplate;
    }

    /**
     * 配置RabbitMQ监听器容器工厂
     * <p>
     * 设置消息转换器用于消费者反序列化消息
     * </p>
     *
     * @param connectionFactory RabbitMQ连接工厂
     * @return SimpleRabbitListenerContainerFactory实例
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }

    // ==================== 代码评测队列 ====================

    /**
     * 代码评测提交队列
     * <p>
     * 用于接收用户提交的代码评测请求
     * </p>
     *
     * @return 队列实例
     */
    @Bean
    public Queue judgeSubmitQueue() {
        return new Queue(RabbitMQConstant.JUDGE_SUBMIT_QUEUE, true);
    }

    /**
     * 代码评测提交交换机（Direct类型）
     *
     * @return 交换机实例
     */
    @Bean
    public DirectExchange judgeSubmitExchange() {
        return new DirectExchange(RabbitMQConstant.JUDGE_SUBMIT_EXCHANGE, true, false);
    }

    /**
     * 绑定代码评测提交队列到交换机
     *
     * @return 绑定关系
     */
    @Bean
    public Binding judgeSubmitBinding() {
        return BindingBuilder.bind(judgeSubmitQueue())
                .to(judgeSubmitExchange())
                .with(RabbitMQConstant.JUDGE_SUBMIT_ROUTING_KEY);
    }

    /**
     * 代码评测结果队列
     *
     * @return 队列实例
     */
    @Bean
    public Queue judgeResultQueue() {
        return new Queue(RabbitMQConstant.JUDGE_RESULT_QUEUE, true);
    }

    /**
     * 代码评测结果交换机
     *
     * @return 交换机实例
     */
    @Bean
    public DirectExchange judgeResultExchange() {
        return new DirectExchange(RabbitMQConstant.JUDGE_RESULT_EXCHANGE, true, false);
    }

    /**
     * 绑定代码评测结果队列到交换机
     *
     * @return 绑定关系
     */
    @Bean
    public Binding judgeResultBinding() {
        return BindingBuilder.bind(judgeResultQueue())
                .to(judgeResultExchange())
                .with(RabbitMQConstant.JUDGE_RESULT_ROUTING_KEY);
    }

    // ==================== 考试提交队列 ====================

    /**
     * 考试提交队列
     * <p>
     * 用于异步处理考试答卷提交和自动评分
     * </p>
     *
     * @return 队列实例
     */
    @Bean
    public Queue examSubmitQueue() {
        return new Queue(RabbitMQConstant.EXAM_SUBMIT_QUEUE, true);
    }

    /**
     * 考试提交交换机
     *
     * @return 交换机实例
     */
    @Bean
    public DirectExchange examSubmitExchange() {
        return new DirectExchange(RabbitMQConstant.EXAM_SUBMIT_EXCHANGE, true, false);
    }

    /**
     * 绑定考试提交队列到交换机
     *
     * @return 绑定关系
     */
    @Bean
    public Binding examSubmitBinding() {
        return BindingBuilder.bind(examSubmitQueue())
                .to(examSubmitExchange())
                .with(RabbitMQConstant.EXAM_SUBMIT_ROUTING_KEY);
    }

    // ==================== 订单超时队列 ====================

    /**
     * 订单超时队列（死信队列）
     * <p>
     * 用于处理超时未支付的订单自动关闭
     * </p>
     *
     * @return 队列实例
     */
    @Bean
    public Queue orderTimeoutQueue() {
        return QueueBuilder.durable(RabbitMQConstant.ORDER_TIMEOUT_QUEUE).build();
    }

    /**
     * 订单超时交换机（Direct类型）
     *
     * @return 交换机实例
     */
    @Bean
    public DirectExchange orderTimeoutExchange() {
        return new DirectExchange(RabbitMQConstant.ORDER_TIMEOUT_EXCHANGE, true, false);
    }

    /**
     * 绑定订单超时队列到交换机
     *
     * @return 绑定关系
     */
    @Bean
    public Binding orderTimeoutBinding() {
        return BindingBuilder.bind(orderTimeoutQueue())
                .to(orderTimeoutExchange())
                .with(RabbitMQConstant.ORDER_TIMEOUT_ROUTING_KEY);
    }

    // ==================== 视频转码队列 ====================

    /**
     * 视频转码队列
     * <p>
     * 用于异步处理上传视频的转码任务
     * </p>
     *
     * @return 队列实例
     */
    @Bean
    public Queue videoTranscodeQueue() {
        return new Queue(RabbitMQConstant.VIDEO_TRANSCODE_QUEUE, true);
    }

    /**
     * 视频转码交换机
     *
     * @return 交换机实例
     */
    @Bean
    public DirectExchange videoTranscodeExchange() {
        return new DirectExchange(RabbitMQConstant.VIDEO_TRANSCODE_EXCHANGE, true, false);
    }

    /**
     * 绑定视频转码队列到交换机
     *
     * @return 绑定关系
     */
    @Bean
    public Binding videoTranscodeBinding() {
        return BindingBuilder.bind(videoTranscodeQueue())
                .to(videoTranscodeExchange())
                .with(RabbitMQConstant.VIDEO_TRANSCODE_ROUTING_KEY);
    }

    // ==================== 消息通知队列 ====================

    /**
     * 消息通知队列
     * <p>
     * 用于异步发送站内消息、短信通知、邮件通知等
     * </p>
     *
     * @return 队列实例
     */
    @Bean
    public Queue messageNotifyQueue() {
        return new Queue(RabbitMQConstant.MESSAGE_NOTIFY_QUEUE, true);
    }

    /**
     * 消息通知交换机（Fanout类型，广播模式）
     *
     * @return 交换机实例
     */
    @Bean
    public FanoutExchange messageNotifyExchange() {
        return new FanoutExchange(RabbitMQConstant.MESSAGE_NOTIFY_EXCHANGE, true, false);
    }

    /**
     * 绑定消息通知队列到交换机
     *
     * @return 绑定关系
     */
    @Bean
    public Binding messageNotifyBinding() {
        return BindingBuilder.bind(messageNotifyQueue())
                .to(messageNotifyExchange());
    }

    // ==================== 竞赛判题队列 ====================

    /**
     * 竞赛判题队列
     * <p>
     * 用于异步处理竞赛作品提交的 Scratch 判题任务
     * </p>
     *
     * @return 队列实例
     */
    @Bean
    public Queue competitionJudgeQueue() {
        return new Queue(RabbitMQConstant.COMPETITION_JUDGE_QUEUE, true);
    }

    /**
     * 竞赛判题交换机（Direct类型）
     *
     * @return 交换机实例
     */
    @Bean
    public DirectExchange competitionJudgeExchange() {
        return new DirectExchange(RabbitMQConstant.COMPETITION_JUDGE_EXCHANGE, true, false);
    }

    /**
     * 绑定竞赛判题队列到交换机
     *
     * @return 绑定关系
     */
    @Bean
    public Binding competitionJudgeBinding() {
        return BindingBuilder.bind(competitionJudgeQueue())
                .to(competitionJudgeExchange())
                .with(RabbitMQConstant.COMPETITION_JUDGE_ROUTING_KEY);
    }
}
