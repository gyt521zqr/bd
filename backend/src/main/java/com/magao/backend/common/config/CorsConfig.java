package com.magao.backend.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS跨域配置类
 * <p>
 * 功能说明：
 * 通过Filter方式配置CORS跨域，作为WebMvcConfig的补充，
 * 确保所有请求（包括非Spring MVC处理的请求）都能正确处理跨域。
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Configuration
public class CorsConfig {

    /**
     * 创建CORS过滤器Bean
     * <p>
     * 配置详细的跨域策略：
     * - 允许所有来源
     * - 允许所有请求头
     * - 允许携带凭证
     * - 预检请求缓存1小时
     * </p>
     *
     * @return CorsFilter实例
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许所有来源（生产环境应限制具体域名）
        config.addAllowedOriginPattern("*");
        // 允许所有请求头
        config.addAllowedHeader("*");
        // 允许所有HTTP方法
        config.addAllowedMethod("*");
        // 允许携带Cookie等凭证信息
        config.setAllowCredentials(true);
        // 预检请求缓存时间（秒）
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有路径应用CORS配置
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
