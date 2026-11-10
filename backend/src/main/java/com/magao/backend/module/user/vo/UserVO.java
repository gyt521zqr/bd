package com.magao.backend.module.user.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户视图对象
 * <p>
 * 用于返回用户信息给前端，不包含密码等敏感信息
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
public class UserVO {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String phone;
    private String email;
    private String avatar;
    private Integer roleId;
    private String roleName;
    private Integer status;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private LocalDateTime createTime;
}
