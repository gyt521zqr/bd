package com.magao.backend.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步线程池配置类
 * <p>
 * 功能说明：
 * 1. 配置异步任务线程池
 * 2. 用于处理异步任务（如发送邮件、消息通知、日志记录等）
 * 3. 合理设置核心线程数、最大线程数、队列容量等参数
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Configuration
@EnableAsync
public class ThreadPoolConfig {

    /**
     * 创建异步任务执行器
     * <p>
     * 线程池参数说明：
     * - 核心线程数：CPU核心数（保持活跃处理任务）
     * - 最大线程数：CPU核心数 * 2（高峰期扩展）
     * - 队列容量：200（缓冲等待任务）
     * - 空闲存活时间：60秒（非核心线程回收时间）
     * - 线程名前缀：magao-async-（便于日志排查）
     * - 拒绝策略：CallerRunsPolicy（由调用线程执行，不丢弃任务）
     * </p>
     *
     * @return 线程池执行器实例
     */
    @Bean(name = "magaoAsyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(corePoolSize);
        // 最大线程数
        executor.setMaxPoolSize(corePoolSize * 2);
        // 队列容量
        executor.setQueueCapacity(200);
        // 非核心线程空闲存活时间（秒）
        executor.setKeepAliveSeconds(60);
        // 线程名前缀
        executor.setThreadNamePrefix("magao-async-");
        // 拒绝策略：由调用线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务完成后关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 等待终止时间（秒）
        executor.setAwaitTerminationSeconds(60);
        // 初始化
        executor.initialize();
        return executor;
    }
}
