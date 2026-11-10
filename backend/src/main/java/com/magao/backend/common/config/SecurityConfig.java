package com.magao.backend.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security安全配置类
 * <p>
 * 功能说明：
 * 1. 配置HTTP安全策略
 * 2. 禁用CSRF（前后端分离不需要）
 * 3. 禁用Session（使用JWT无状态认证）
 * 4. 放行公开接口
 * 5. 添加JWT认证过滤器
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 安全过滤链配置
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                    com.magao.backend.common.filter.JwtAuthFilter jwtAuthFilter) throws Exception {
        http
            // 禁用CSRF（前后端分离，使用JWT）
            .csrf(csrf -> csrf.disable())
            // 禁用Session（无状态认证）
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 授权配置
            .authorizeHttpRequests(auth -> auth
                // 认证相关接口放行
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/file/public/**").permitAll()
                // CMS 公开读接口：仅放行 GET。
                // 这些路径下同时存在后台写接口（POST/PUT/DELETE），
                // 写操作必须携带 Token 且由 @RequireRole / RequireRoleAspect 校验管理员角色，
                // 不能再用整段 permitAll 让游客越权写入。
                .requestMatchers(HttpMethod.GET, "/api/news/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/partners/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/homepage/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/about/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/banner/**").permitAll()
                // 系统公开配置/公告（仅读；PUT /api/system/config 由 @RequireRole(ADMIN) 保护）
                .requestMatchers(HttpMethod.GET, "/api/system/notices").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/system/site-config").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/system/config/*").permitAll()
                // 前台公开查询
                .requestMatchers(HttpMethod.GET, "/api/course/list").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/course/detail/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/course/categories").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/exam/list").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/exam/detail/**").permitAll()
                // OSS 公共读取（上传后的文件回显/下载，免登录）
                .requestMatchers(HttpMethod.GET, "/api/oss/raw/**").permitAll()
                // API接口需要认证
                .requestMatchers("/api/**").authenticated()
                // 非API路径全部放行（前端SPA路由和静态资源）
                .anyRequest().permitAll()
            )
            // 在UsernamePasswordAuthenticationFilter之前添加JWT过滤器
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 密码编码器（预留，当前使用MD5+Salt）
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
