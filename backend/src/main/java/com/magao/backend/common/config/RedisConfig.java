package com.magao.backend.common.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis配置类
 * <p>
 * 功能说明：
 * 1. 配置RedisTemplate的序列化方式
 * 2. Key使用String序列化，Value使用JSON序列化
 * 3. 解决Redis存储对象乱码问题
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Configuration
public class RedisConfig {

    /**
     * 配置自定义RedisTemplate
     * <p>
     * 使用String序列化器序列化Key，使用Jackson JSON序列化器序列化Value，
     * 解决默认JDK序列化导致的存储内容不可读问题。
     * </p>
     *
     * @param connectionFactory Redis连接工厂
     * @return 配置好的RedisTemplate实例
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 设置连接工厂
        template.setConnectionFactory(connectionFactory);

        // 创建Jackson JSON序列化器
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL);
        Jackson2JsonRedisSerializer<Object> jsonSerializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);

        // 创建String序列化器
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // 设置Key的序列化方式为String
        template.setKeySerializer(stringSerializer);
        // 设置Hash Key的序列化方式为String
        template.setHashKeySerializer(stringSerializer);
        // 设置Value的序列化方式为JSON
        template.setValueSerializer(jsonSerializer);
        // 设置Hash Value的序列化方式为JSON
        template.setHashValueSerializer(jsonSerializer);

        // 初始化参数设置
        template.afterPropertiesSet();
        return template;
    }

    /**
     * 配置StringRedisTemplate
     * <p>
     * 专门用于处理String类型的Key-Value操作
     * </p>
     *
     * @param connectionFactory Redis连接工厂
     * @return StringRedisTemplate实例
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
