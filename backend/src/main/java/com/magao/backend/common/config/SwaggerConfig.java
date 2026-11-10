package com.magao.backend.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI / Swagger文档配置类
 * <p>
 * 功能说明：
 * 1. 配置API文档基本信息（标题、描述、版本等）
 * 2. 配置JWT Token认证方式
 * 3. 配置全局安全要求
 * </p>
 * <p>
 * 访问地址：
 * - Swagger UI: http://localhost:8080/swagger-ui.html
 * - API Docs: http://localhost:8080/v3/api-docs
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Configuration
public class SwaggerConfig {

    /**
     * 创建OpenAPI配置Bean
     * <p>
     * 配置API文档的基本信息，包括：
     * - 标题：Magao编程教育平台API文档
     * - 描述：提供完整的RESTful API接口文档
     * - 版本：v1.0.0
     * - 联系方式
     * - JWT Bearer Token认证
     * </p>
     *
     * @return OpenAPI配置实例
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                // API文档基本信息
                .info(new Info()
                        .title("Magao编程教育平台API文档")
                        .description("Magao编程教育平台后端RESTful API接口文档，涵盖用户管理、课程管理、考试系统、代码评测、支付系统等模块")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Magao开发团队")
                                .email("dev@magao.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                // JWT安全配置
                .addSecurityItem(new SecurityRequirement().addList("Bearer Token"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Token",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("输入JWT Token进行身份认证")));
    }
}
