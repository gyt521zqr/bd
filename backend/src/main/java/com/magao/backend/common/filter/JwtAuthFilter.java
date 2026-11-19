package com.magao.backend.common.filter;

import com.magao.backend.common.constant.RedisKeyConstant;
import com.magao.backend.common.exception.AuthException;
import com.magao.backend.common.result.ErrorCode;
import com.magao.backend.common.util.IpUtil;
import com.magao.backend.common.util.JwtUtil;
import com.magao.backend.common.util.RedisUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * JWT认证过滤器
 * <p>
 * 继承OncePerRequestFilter，确保每个请求只过滤一次。
 * 功能：
 * 1. 从请求头中提取JWT Token
 * 2. 验证Token的有效性
 * 3. 检查Token是否在Redis中存在（单设备登录限制）
 * 4. 将用户信息设置到SecurityContext中
 * </p>
 * <p>
 * 白名单路径（不需要Token验证）：
 * - /api/auth/**：认证相关接口
 * - /swagger-ui/**：Swagger文档
 * - /v3/api-docs/**：API文档
 * - /favicon.ico：网站图标
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    /** JWT工具类 */
    private final JwtUtil jwtUtil;

    /** Redis工具类 */
    private final RedisUtil redisUtil;

    /** 路径匹配器 */
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    /**
     * 全方法白名单：这些路径本身不产生受保护的写操作（登录/注册、静态资源、成绩查询等）。
     */
    private static final List<String> FULL_WHITE_LIST = Arrays.asList(
            "/api/auth/**",
            "/api/file/public/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/favicon.ico",
            "/error",
            "/actuator/**",
            // 支付异步回调：支付机构不携带本站 JWT，安全性由网关验签保证（P0-02）
            "/api/payment/notify/**"
    );

    /**
     * 只读白名单：这些路径下同时存在「公开 GET 读接口」与「后台写接口」
     * （例如 /api/banner 的 GET 列表 与 POST 新建）。
     * 仅对 GET/HEAD/OPTIONS 放行；POST/PUT/DELETE 必须携带 Token，
     * 并由 {@code RequireRoleAspect} 校验管理员角色，避免游客越权写入。
     */
    private static final List<String> READ_ONLY_WHITE_LIST = Arrays.asList(
            // CMS公开读接口
            "/api/news/**",
            "/api/categories/**",
            "/api/partners/**",
            "/api/homepage/**",
            "/api/about/**",
            "/api/banner/**",
            // 系统公开配置/公告
            "/api/system/notices",
            "/api/system/site-config",
            "/api/system/config/*",
            // 前台公开查询
            "/api/course/list",
            "/api/course/detail/**",
            "/api/course/categories",
            "/api/exam/list",
            "/api/exam/detail/**",
            "/api/oss/raw/**"
    );

    /** 不需要认证的路径前缀（非API路径） */
    private static final String API_PREFIX = "/api/";

    /**
     * 过滤器核心方法
     * <p>
     * 对每个请求执行JWT Token验证：
     * 1. 白名单路径直接放行
     * 2. 提取并验证Token
     * 3. 设置SecurityContext认证信息
     * </p>
     *
     * @param request     HTTP请求
     * @param response    HTTP响应
     * @param filterChain 过滤器链
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 获取请求路径
        String requestUri = request.getRequestURI();

        // 白名单路径放行（CMS 类路径仅放行读方法，写方法必须带 Token）
        if (isWhiteListed(requestUri, request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // 非API路径直接放行（前端SPA路由和静态资源）
        if (!requestUri.startsWith(API_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 从请求头获取Token
        String token = extractToken(request);

        // Token为空则返回未认证
        if (!StringUtils.hasText(token)) {
            sendUnauthorizedResponse(response, "未登录或Token已过期");
            return;
        }

        try {
            // 验证Token有效性
            if (!jwtUtil.validateToken(token)) {
                sendUnauthorizedResponse(response, "Token无效或已过期");
                return;
            }

            // 获取用户ID
            Long userId = jwtUtil.getUserId(token);
            if (userId == null) {
                sendUnauthorizedResponse(response, "Token中用户信息无效");
                return;
            }

            // 检查Token是否在Redis中存在（单设备登录限制）
            String redisTokenKey = RedisKeyConstant.USER_TOKEN_PREFIX + userId;
            Object cachedToken = redisUtil.get(redisTokenKey);
            if (cachedToken == null || !token.equals(cachedToken.toString())) {
                sendUnauthorizedResponse(response, "账号已在其他设备登录，请重新登录");
                return;
            }

            // 将用户信息设置到SecurityContext
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, java.util.Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 放行请求
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("JWT认证失败：{}", e.getMessage());
            sendUnauthorizedResponse(response, "认证失败");
        }
    }

    /**
     * 从请求头中提取Token
     * <p>
     * 支持格式：Bearer {token}
     * </p>
     *
     * @param request HTTP请求
     * @return Token字符串，未找到返回null
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 判断请求是否在白名单中。
     *
     * @param requestUri 请求URI
     * @param httpMethod HTTP 方法
     * @return true 表示无需 Token 即可通行
     */
    private boolean isWhiteListed(String requestUri, String httpMethod) {
        if (FULL_WHITE_LIST.stream().anyMatch(p -> PATH_MATCHER.match(p, requestUri))) {
            return true;
        }
        boolean readOnly = "GET".equalsIgnoreCase(httpMethod)
                || "HEAD".equalsIgnoreCase(httpMethod)
                || "OPTIONS".equalsIgnoreCase(httpMethod);
        if (!readOnly) {
            return false;
        }
        return READ_ONLY_WHITE_LIST.stream().anyMatch(p -> PATH_MATCHER.match(p, requestUri));
    }

    /**
     * 发送未认证响应
     *
     * @param response HTTP响应
     * @param message 错误消息
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"" + message + "\",\"timestamp\":" +
                System.currentTimeMillis() + "}");
    }
}
