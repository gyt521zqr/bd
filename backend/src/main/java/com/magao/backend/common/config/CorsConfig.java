package com.magao.backend.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

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
     * P1-16：允许的来源白名单，逗号分隔。默认 *（仅本地开发）；
     * 生产通过 CORS_ALLOWED_ORIGINS 环境变量注入具体域名，如 https://wukonghuwei.cn。
     */
    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;

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
        // P1-16：生产用白名单，开发默认 *
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toList();
        if (origins.size() == 1 && "*".equals(origins.get(0))) {
            config.addAllowedOriginPattern("*");
            config.setAllowCredentials(false); // credentials + * 非法组合，通配时关闭凭证
        } else {
            origins.forEach(config::addAllowedOriginPattern);
            config.setAllowCredentials(true);
        }
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        // 预检请求缓存时间（秒）
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有路径应用CORS配置
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
