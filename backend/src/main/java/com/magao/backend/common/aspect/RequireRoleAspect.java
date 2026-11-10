package com.magao.backend.common.aspect;

import com.magao.backend.common.annotation.RequireRole;
import com.magao.backend.common.exception.AuthException;
import com.magao.backend.module.user.entity.User;
import com.magao.backend.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 角色权限切面 —— 让 {@link RequireRole} 注解真正生效。
 *
 * <p>此前项目中 40+ 处 @RequireRole 只是装饰：既没有切面也没有拦截器读取它，
 * 导致任意已登录用户（甚至被 JWT 白名单放行的游客）都能调用管理员写接口。
 * 本切面在 Controller 方法执行前从 SecurityContext 取出 JwtAuthFilter 写入的 userId，
 * 查询 sys_user.role_type 并与注解允许的角色比对。</p>
 *
 * <p>支持方法级与类级注解，方法级优先。用户角色取 UserRoleEnum 的枚举名
 * （库里 role_type 存 "STUDENT"/"TEACHER"/"OPERATOR"/"ADMIN" 字符串）。</p>
 *
 * <p>未登录抛 AuthException（由 GlobalExceptionHandler 转 401）；
 * 角色不足抛 AccessDeniedException（转 403）。</p>
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RequireRoleAspect {

    private final UserMapper userMapper;

    @Around("@annotation(com.magao.backend.common.annotation.RequireRole) || "
            + "@within(com.magao.backend.common.annotation.RequireRole)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        RequireRole rule = resolveRule(joinPoint);
        if (rule == null || rule.value().length == 0) {
            return joinPoint.proceed();
        }

        Long userId = currentUserId();
        if (userId == null) {
            throw new AuthException("未登录或Token已过期");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new AuthException("账号不存在或已被删除");
        }

        String role = user.getRoleType() == null ? "" : user.getRoleType().trim();
        boolean allowed = Arrays.stream(rule.value())
                .anyMatch(r -> r.name().equalsIgnoreCase(role));

        if (!allowed) {
            String expected = Arrays.stream(rule.value())
                    .map(Enum::name).collect(Collectors.joining("/"));
            log.warn("角色校验拒绝: userId={}, role={}, required={}, method={}",
                    userId, role, expected, joinPoint.getSignature().toShortString());
            throw new AccessDeniedException("权限不足，需要角色：" + expected);
        }

        return joinPoint.proceed();
    }

    /** 方法级注解优先，其次类级注解 */
    private RequireRole resolveRule(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?> targetClass = AopUtils.getTargetClass(joinPoint.getTarget());
        Method specific = AopUtils.getMostSpecificMethod(signature.getMethod(), targetClass);

        RequireRole rule = AnnotatedElementUtils.findMergedAnnotation(specific, RequireRole.class);
        if (rule == null) {
            rule = AnnotatedElementUtils.findMergedAnnotation(targetClass, RequireRole.class);
        }
        return rule;
    }

    /** JwtAuthFilter 把 userId 作为 Authentication 的 principal 写入 SecurityContext */
    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof Number) {
            return ((Number) principal).longValue();
        }
        if (principal instanceof String) {
            try {
                return Long.parseLong((String) principal);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
