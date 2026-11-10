package com.magao.backend.common.annotation;

import com.magao.backend.common.enums.UserRoleEnum;

import java.lang.annotation.*;

/**
 * 角色权限注解
 * <p>
 * 用于Controller类/方法级别的角色权限控制，
 * 限制只有指定角色的用户才能访问该接口。
 * 由 {@code RequireRoleAspect} 统一拦截校验；方法级注解优先于类级注解。
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * &#64;RequireRole({UserRoleEnum.ADMIN, UserRoleEnum.TEACHER})
 * &#64;GetMapping("/admin/users")
 * public Result listUsers(...) { ... }
 * </pre>
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {

    /**
     * 允许访问的角色列表
     * <p>
     * 只要用户拥有其中任一角色即可访问
     * </p>
     *
     * @return 角色枚举数组
     */
    UserRoleEnum[] value();
}
