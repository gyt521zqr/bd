package com.magao.backend.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus配置类
 * <p>
 * 功能说明：
 * 1. 配置分页插件 - 支持MySQL分页查询
 * 2. 配置乐观锁插件 - 支持版本号并发控制
 * 3. 配置自动填充处理器 - 自动填充创建时间和更新时间
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * MyBatis-Plus拦截器配置
     * <p>
     * 注册分页插件和乐观锁插件到拦截器链中
     * </p>
     *
     * @return MybatisPlusInterceptor 拦截器实例
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加乐观锁插件（需要在分页插件之前添加）
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        // 添加分页插件，指定数据库类型为MySQL
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
