package com.magao.backend.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 登录数据传输对象
 * <p>
 * 用于接收用户登录请求的参数：
 * - 支持用户名密码登录
 * - 支持手机号验证码登录
 * - 支持微信登录
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
public class LoginDTO {

    /** 用户名（用户名密码登录时必填） */
    @Size(min = 3, max = 50, message = "用户名长度为3-50个字符")
    private String username;

    /** 密码（用户名密码登录时必填） */
    @Size(min = 6, max = 100, message = "密码长度为6-100个字符")
    private String password;

    /** 手机号（短信登录时必填） */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 短信验证码（短信登录时必填） */
    @Size(min = 4, max = 6, message = "验证码长度为4-6位")
    private String code;

    /** 登录类型：password-密码登录，sms-短信登录，wechat-微信登录 */
    @NotBlank(message = "登录类型不能为空")
    private String loginType;
}
