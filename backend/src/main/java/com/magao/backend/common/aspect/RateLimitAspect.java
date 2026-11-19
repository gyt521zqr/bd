package com.magao.backend.common.aspect;

import com.magao.backend.common.annotation.RateLimit;
import com.magao.backend.common.constant.RedisKeyConstant;
import com.magao.backend.common.exception.BusinessException;
import com.magao.backend.common.util.IpUtil;
import com.magao.backend.common.util.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 限流切面
 * <p>
 * 基于@RateLimit注解实现接口级别的限流控制，
 * 使用Redis Lua脚本实现滑动窗口限流算法，
 * 保证原子性和准确性。
 * </p>
 * <p>
 * 限流维度：IP地址 + 接口标识
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    /** Redis操作工具 */
    private final RedisUtil redisUtil;

    /**
     * 环绕通知 - 执行限流检查（固定窗口：increment + expire，原子计数）
     * <p>
     * 在方法执行前检查是否超过限流阈值，超过则抛出 BusinessException 阻止请求执行。
     * 限流维度：IP地址 + 接口标识。IP 取自 IpUtil（直连不信任转发头，防伪造）。
     * </p>
     */
    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        // 获取请求对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }
        HttpServletRequest request = attributes.getRequest();

        // 构建限流Key：rate:limit:{注解key}:{客户端IP}:{窗口序号}
        String clientIp = IpUtil.getClientIp(request);
        long currentTime = System.currentTimeMillis() / 1000;
        String key = RedisKeyConstant.RATE_LIMIT_PREFIX + rateLimit.key() + ":" + clientIp
                + ":" + (currentTime / rateLimit.timeWindow());

        Long requestCount = redisUtil.increment(key);
        if (requestCount != null && requestCount == 1) {
            redisUtil.expire(key, rateLimit.timeWindow(), java.util.concurrent.TimeUnit.SECONDS);
        }

        // 判断是否超过限流阈值
        if (requestCount != null && requestCount > rateLimit.maxCount()) {
            log.warn("限流拦截：ip={}, key={}, count={}", clientIp, rateLimit.key(), requestCount);
            throw new BusinessException(429, rateLimit.message());
        }

        return joinPoint.proceed();
    }
}
