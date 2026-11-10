package com.magao.backend.common.base;

/**
 * 控制器基类
 * <p>
 * 所有Controller的父类，提供公共的Controller方法。
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
public class BaseController {

    /**
     * 获取当前登录用户ID
     * <p>
     * 从SecurityContext或请求头中获取当前登录用户的ID
     * </p>
     *
     * @return 当前登录用户ID
     */
    protected Long getCurrentUserId() {
        // 从SecurityContext获取，具体实现依赖JWT过滤器设置
        org.springframework.security.core.Authentication authentication =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * 获取当前登录用户名
     *
     * @return 当前登录用户名
     */
    protected String getCurrentUsername() {
        org.springframework.security.core.Authentication authentication =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null) {
            return authentication.getName();
        }
        return null;
    }
}
