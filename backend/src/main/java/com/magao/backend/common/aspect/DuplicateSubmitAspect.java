package com.magao.backend.common.aspect;

import com.magao.backend.common.annotation.PreventDuplicateSubmit;
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

import java.util.concurrent.TimeUnit;

/**
 * 防重复提交切面
 * <p>
 * 基于@PreventDuplicateSubmit注解实现防重复提交功能，
 * 使用Redis存储请求指纹（MD5(请求参数+IP+接口)），
 * 在指定时间窗口内如果发现相同指纹则拒绝请求。
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DuplicateSubmitAspect {

    /** Redis操作工具 */
    private final RedisUtil redisUtil;

    /**
     * 环绕通知 - 检查是否重复提交
     * <p>
     * 1. 根据请求参数、IP、接口路径生成请求指纹
     * 2. 检查Redis中是否存在该指纹
     * 3. 如果存在则拒绝请求，否则放行并设置指纹过期时间
     * </p>
     *
     * @param joinPoint 切点
     * @param preventDuplicateSubmit 防重复提交注解
     * @return 方法执行结果
     * @throws Throwable 异常
     */
    @Around("@annotation(preventDuplicateSubmit)")
    public Object around(ProceedingJoinPoint joinPoint, PreventDuplicateSubmit preventDuplicateSubmit) throws Throwable {
        // 获取请求对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }
        HttpServletRequest request = attributes.getRequest();

        // 生成请求指纹：基于IP + 请求URI + 请求参数
        String clientIp = IpUtil.getClientIp(request);
        String requestUri = request.getRequestURI();
        String params = request.getQueryString() != null ? request.getQueryString() : "";
        String fingerprint = com.magao.backend.common.util.CryptoUtil.md5(clientIp + ":" + requestUri + ":" + params);

        // 构建Redis Key
        String redisKey = RedisKeyConstant.DUPLICATE_SUBMIT_PREFIX + fingerprint;

        // 检查是否已存在（使用setIfAbsent实现原子性）
        Boolean isFirst = redisUtil.setIfAbsent(redisKey, "1", preventDuplicateSubmit.timeWindow(), TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(isFirst)) {
            log.warn("防重复提交拦截：ip={}, uri={}", clientIp, requestUri);
            throw new BusinessException(429, preventDuplicateSubmit.message());
        }

        return joinPoint.proceed();
    }
}
