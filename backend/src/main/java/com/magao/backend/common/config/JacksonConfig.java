package com.magao.backend.common.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson全局配置类
 * <p>
 * 功能说明：
 * 1. 配置日期时间格式化规则
 * 2. 配置空值序列化处理
 * 3. 配置反序列化容错机制
 * 4. 注册Java 8时间模块
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Configuration
public class JacksonConfig {

    /** 日期格式 */
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    /** 时间格式 */
    private static final String TIME_FORMAT = "HH:mm:ss";
    /** 日期时间格式 */
    private static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 配置全局ObjectMapper
     * <p>
     * 统一日期格式化、空值处理和反序列化容错策略
     * </p>
     *
     * @return 自定义配置的ObjectMapper实例
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // 注册Java 8时间模块
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // LocalDateTime序列化和反序列化
        javaTimeModule.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATETIME_FORMAT)));
        javaTimeModule.addDeserializer(LocalDateTime.class,
                new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATETIME_FORMAT)));

        // LocalDate序列化和反序列化
        javaTimeModule.addSerializer(LocalDate.class,
                new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_FORMAT)));
        javaTimeModule.addDeserializer(LocalDate.class,
                new LocalDateDeserializer(DateTimeFormatter.ofPattern(DATE_FORMAT)));

        // LocalTime序列化和反序列化
        javaTimeModule.addSerializer(LocalTime.class,
                new LocalTimeSerializer(DateTimeFormatter.ofPattern(TIME_FORMAT)));
        javaTimeModule.addDeserializer(LocalTime.class,
                new LocalTimeDeserializer(DateTimeFormatter.ofPattern(TIME_FORMAT)));

        objectMapper.registerModule(javaTimeModule);

        // 序列化时忽略null值
        objectMapper.setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);

        // 反序列化时忽略未知属性（容错处理）
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 序列化时不将日期写为时间戳
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // 反序列化时对枚举大小写不敏感
        objectMapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);

        return objectMapper;
    }
}
