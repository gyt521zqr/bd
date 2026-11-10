package com.magao.backend.common.filter;

import com.magao.backend.common.constant.RedisKeyConstant;
import com.magao.backend.common.util.IpUtil;
import com.magao.backend.common.util.RedisUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * IP级限流过滤器
 * <p>
 * 对每个IP地址进行全局限流，防止恶意请求和DDoS攻击。
 * 在指定时间窗口内，同一IP的请求次数超过阈值则拒绝。
 * </p>
 * <p>
 * 限流规则：
 * - 每个IP每分钟最多100次请求
 * - 超过限制返回429状态码
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    /** Redis工具类 */
    private final RedisUtil redisUtil;

    /** 每分钟最大请求数 */
    private static final int MAX_REQUESTS_PER_MINUTE = 100;

    /** 限流时间窗口（秒） */
    private static final int TIME_WINDOW_SECONDS = 60;

    /**
     * 过滤器核心方法
     * <p>
     * 对每个请求进行IP级限流检查
     * </p>
     *
     * @param request     HTTP请求
     * @param response    HTTP响应
     * @param filterChain 过滤器链
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String clientIp = IpUtil.getClientIp(request);
        String redisKey = RedisKeyConstant.RATE_IP_PREFIX + clientIp;

        try {
            // 递增请求计数
            Long count = redisUtil.increment(redisKey);

            // 第一次访问时设置过期时间
            if (count != null && count == 1) {
                redisUtil.expire(redisKey, TIME_WINDOW_SECONDS, TimeUnit.SECONDS);
            }

            // 判断是否超过限流阈值
            if (count != null && count > MAX_REQUESTS_PER_MINUTE) {
                log.warn("IP限流拦截：ip={}, count={}", clientIp, count);
                response.setStatus(429);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\",\"timestamp\":" +
                        System.currentTimeMillis() + "}");
                return;
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("IP限流过滤器异常：{}", e.getMessage());
            // 限流过滤器异常不影响正常请求
            filterChain.doFilter(request, response);
        }
    }
}
