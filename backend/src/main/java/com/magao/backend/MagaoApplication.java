package com.magao.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Magao编程教育平台 - 后端启动类
 * <p>
 * 功能说明：
 * 1. 启动SpringBoot应用
 * 2. 自动扫描Mapper接口
 * 3. 开启异步任务支持
 * 4. 开启定时任务支持
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@SpringBootApplication
@MapperScan("com.magao.backend.module.*.mapper")
@EnableAsync
@EnableScheduling
public class MagaoApplication {

    /**
     * 应用程序入口方法
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(MagaoApplication.class, args);
    }
}
