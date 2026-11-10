package com.magao.backend.module.user.vo;

import lombok.Data;

/**
 * 登录响应视图对象
 * <p>
 * 包含登录成功后的Token信息和用户基本信息
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
public class LoginVO {
    /** 访问Token */
    private String accessToken;
    /** 刷新Token */
    private String refreshToken;
    /** Token类型 */
    private String tokenType;
    /** 用户信息 */
    private UserVO user;
}
