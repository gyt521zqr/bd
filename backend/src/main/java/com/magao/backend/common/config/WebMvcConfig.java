package com.magao.backend.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvc配置类
 * <p>
 * 功能说明：
 * 1. 配置CORS跨域策略
 * 2. 配置静态资源处理
 * 3. 配置拦截器
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 配置静态资源处理
     * <p>
     * 添加Swagger UI等静态资源映射
     * </p>
     * <p>
     * 注意：CORS跨域配置已移至 CorsConfig.java（Filter方式），
     * 避免双重CORS配置导致浏览器拒绝请求。
     * </p>
     *
     * @param registry 资源处理器注册器
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Swagger UI静态资源映射
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/");
    }
}
