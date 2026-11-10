package com.magao.backend.common.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magao.backend.common.annotation.LogOperation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 操作日志切面
 * <p>
 * 基于@LogOperation注解自动记录操作日志，
 * 包括：操作模块、操作类型、操作描述、请求参数、执行时间等。
 * </p>
 * <p>
 * 日志输出格式：[模块-操作类型] 描述 | 耗时: xxxms | 参数: xxx
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    /** JSON序列化工具 */
    private final ObjectMapper objectMapper;

    /**
     * 环绕通知 - 记录操作日志
     * <p>
     * 在方法执行前后记录日志信息：
     * 1. 执行前：记录请求参数
     * 2. 执行后：记录执行耗时
     * </p>
     *
     * @param joinPoint 切点
     * @param logOperation 日志注解
     * @return 方法执行结果
     * @throws Throwable 异常
     */
    @Around("@annotation(logOperation)")
    public Object around(ProceedingJoinPoint joinPoint, LogOperation logOperation) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String requestUri = "";
        String requestMethod = "";
        String clientIp = "";
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            requestUri = request.getRequestURI();
            requestMethod = request.getMethod();
            clientIp = request.getRemoteAddr();
        }

        // 获取方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = signature.getName();

        // 记录请求参数
        String params = "";
        if (logOperation.recordParam()) {
            params = buildParams(joinPoint, signature);
        }

        // 记录开始日志
        log.info("[{}-{}] {} | URI: {} {} | IP: {} | 参数: {}",
                logOperation.module(), logOperation.type(), logOperation.description(),
                requestMethod, requestUri, clientIp, params);

        // 执行方法
        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            // 记录异常日志
            long elapsedTime = System.currentTimeMillis() - startTime;
            log.error("[{}-{}] {} | 耗时: {}ms | 异常: {}",
                    logOperation.module(), logOperation.type(), logOperation.description(),
                    elapsedTime, e.getMessage());
            throw e;
        }

        // 记录完成日志
        long elapsedTime = System.currentTimeMillis() - startTime;
        String resultStr = "";
        if (logOperation.recordResult() && result != null) {
            try {
                resultStr = objectMapper.writeValueAsString(result);
                // 截断过长的结果
                if (resultStr.length() > 500) {
                    resultStr = resultStr.substring(0, 500) + "...(已截断)";
                }
            } catch (Exception e) {
                resultStr = result.toString();
            }
        }

        log.info("[{}-{}] {} | 耗时: {}ms | 结果: {}",
                logOperation.module(), logOperation.type(), logOperation.description(),
                elapsedTime, resultStr);

        return result;
    }

    /**
     * 构建请求参数字符串
     * <p>
     * 将方法参数序列化为JSON字符串，过滤掉不可序列化的参数类型
     * </p>
     *
     * @param joinPoint 切点
     * @param signature 方法签名
     * @return 参数JSON字符串
     */
    private String buildParams(ProceedingJoinPoint joinPoint, MethodSignature signature) {
        try {
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();
            if (paramNames == null || paramNames.length == 0) {
                return "{}";
            }
            Map<String, Object> paramMap = new HashMap<>();
            for (int i = 0; i < paramNames.length; i++) {
                // 过滤不可序列化的参数（如MultipartFile、HttpServletRequest等）
                if (args[i] instanceof MultipartFile || args[i] instanceof HttpServletRequest
                        || args[i] instanceof jakarta.servlet.http.HttpServletResponse) {
                    paramMap.put(paramNames[i], "[" + args[i].getClass().getSimpleName() + "]");
                } else {
                    paramMap.put(paramNames[i], args[i]);
                }
            }
            String result = objectMapper.writeValueAsString(paramMap);
            if (result.length() > 1000) {
                result = result.substring(0, 1000) + "...(已截断)";
            }
            return result;
        } catch (Exception e) {
            log.warn("序列化请求参数失败：{}", e.getMessage());
            return "{}";
        }
    }
}
